package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.config.i18n.*;
import com.vero.coreprocessor.destinations.dtos.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.model.dto.*;
import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.institution.repository.*;
import com.vero.coreprocessor.keymodule.service.*;
import com.vero.coreprocessor.utils.*;
import jakarta.transaction.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.*;
import org.apache.commons.lang3.tuple.*;
import org.bouncycastle.crypto.*;
import org.jpos.iso.*;
import org.jpos.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

import java.util.*;

import static com.vero.coreprocessor.utils.EncryptionUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DestinationServiceImpl implements DestinationService {
    private final DestinationRepository destinationRepository;
    private final ChannelService channelService;
    private final KeyUtils keyUtils;
    private final InstitutionDestinationRepository institutionDestinationRepository;
    private final InstitutionRepository institutionRepository;


    @Override
    public DestinationResponse registerDestination(RegisterDestination request) {
        log.info("registering destination with request {}", request.toString());
        destinationRepository.findByDestinationName(request.getDestinationName().replace(" ", ""))
                .ifPresent(destination -> {
                    log.error("destination {} already exists", destination.getDestinationName());
                    throw new OmniproApplicationException(MessageCode.DUPLICATE_ENTITY, "destination already exists");
                });

        Destination destination = Destination.builder()
                .destinationName(request.getDestinationName().replace(" ", ""))
                .port(String.valueOf(request.getPort()))
                .zpk(request.getZpk().substring(0, 32))
                .zmk(request.getZmk().substring(0, 32))
                .ip(request.getIp())
                .domain(request.getDomain())
                .status(true)
                .build();
        log.info("creating channel and mux for destination");


        boolean channelMuxConfig = channelService.createChannelMuxConfig(destination);

        if (channelMuxConfig) {
            log.info("channel configs deployed successfully");
            return new DestinationResponse(destinationRepository.save(destination));
        } else
            throw new OmniproApplicationException(MessageCode.FORBIDDEN, "error creating configs");
    }

    @Override
    public DestinationResponse updateDestination(UpdateDestinationDTO request) {
        log.info("updating destination name {} with DTO {}", request.getDestinationName(), request);

        Destination destination = destinationRepository.findByDestinationName(request.getDestinationName()
                .replace(" ", "")).orElseThrow(() -> {
            log.error("destination not found for id {}", request.getDestinationName());
            return new OmniproApplicationException(MessageCode.NOT_FOUND, "destination not found");
        });
        if (request.getParam().containsKey("ip"))
            destination.setIp((String) request.getParam().get("ip"));
        if (request.getParam().containsKey("port"))
            destination.setPort((String) request.getParam().get("port"));

        boolean channelMuxConfig = channelService.updateChannelMuxConfig(destination);
        if (channelMuxConfig) {
            log.info("configs successfully updated");
            return new DestinationResponse(destinationRepository.save(destination));
        } else {
            throw new OmniproApplicationException(MessageCode.FORBIDDEN, "error updating configs");
        }
    }

    @Override
    public Page<DestinationResponse> viewAllDestinations(Pageable pageable, DestinationFilter destinationFilter) {
        QueryBuilder<Destination, DestinationResponse> queryBuilder = QueryBuilder.build(Destination.class, DestinationResponse.class);
        destinationFilter.filter(queryBuilder);
        queryBuilder.orderBy("createdAt", true);
        return queryBuilder.getResult(pageable);
    }

    @Override
    public DestinationResponse viewDestinationById(Long id) {
        return destinationRepository.findById(id)
                .map(DestinationResponse::new)
                .orElseThrow(() -> new OmniproApplicationException(MessageCode.NOT_FOUND, "destination not found"));
    }

    @Override
    public DestinationResponse viewByName(String name) {
        return destinationRepository.findByDestinationName(name)
                .map(DestinationResponse::new)
                .orElseThrow(() -> new OmniproApplicationException(MessageCode.NOT_FOUND, "destination with name: " + name + " not found"));
    }

    @Override
    public List<DestinationResponse> viewByStatus(boolean value) {
        return destinationRepository.findAllByStatus(value).stream()
                .map(DestinationResponse::new)
                .toList();
    }

    @Override
    public Object doDestinationKeyExchange(String destination) {
        log.info("about doing key exchange for destination {}", destination);

        try {

            Destination stations = destinationRepository.findByDestinationName(destination).orElse(null);
            if (stations == null) {
                throw new KeyExchangeException("Stations with stationName " + destination + " not found");
            }

            MUX mux = NameRegistrar.getIfExists("mux." + destination);
            if (mux.isConnected()) {
                ISOMsg request = CheckDestinationUptimeService.createMsg();
                request.set(70, "101");
                request.set(3, "9A0000");

                ISOMsg response = mux.request(request, 30000);
                if (response.hasField(39) && response.getString(39).equals("96")) {
                    log.error("destination key exchange not successful");
                    return "destination key exchange not successful";
                } else {
                    Pair<String, String> sessionKeyKcvPair = this.extractMasterSessionKeyData(response);

                    String sessionKey = sessionKeyKcvPair.getLeft();
                    String sessionKcv = sessionKeyKcvPair.getRight();
                    byte[] sessionKeyBytes;
                    try {
                        sessionKeyBytes = Hex.decodeHex(sessionKey.toCharArray());
                    } catch (DecoderException e) {
                        throw new KeyExchangeException("Unable to decode session key to bytes" + e.getMessage());
                    }


                    char[] charZmk = stations.getZmk().toCharArray();
                    byte[] hexZmk = Hex.decodeHex(charZmk);
                    byte[] clearZpk = tdesDecryptECB(sessionKeyBytes, hexZmk);
                    byte[] zpkKcv = generateKeyCheckValue(clearZpk);
                    String generatedKcv = new String(Hex.encodeHex(zpkKcv));
                    if (!sessionKcv.equalsIgnoreCase(generatedKcv)) {
                        throw new KeyExchangeException("Key check values don't match");
                    }
                    stations.setZpk(Hex.encodeHexString(clearZpk));
                    stations.setZpkkcv(generatedKcv);
                    this.destinationRepository.save(stations);
                    return generatedKcv;
                }
            } else {
                log.error("{} not connected", destination);
                return "destination not connected";
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("error doing key exchange. error is {}", e.getMessage());
            return "error doing key exchange. error is " + e.getMessage();
        }

    }

    @Override
    public Object injectDestinationComponents(HashMap<String, String> request) {
        log.info("about injecting destination ZMK components");

        String combinedKey = keyUtils.formKeyFromThreeComponents((short) 128, request.get("component1"), request.get("component2"), request.get("component3"));
        Destination destination = destinationRepository.findByDestinationName(request.get("destination")).orElseThrow(() -> {
            log.error("destination not found");
            return new OmniproApplicationException(MessageCode.NOT_FOUND, "destination not found");
        });

        destination.setZmk(combinedKey.substring(0, 32));
        destination.setZmkKcv(combinedKey.substring(32, 38));
        return destinationRepository.save(destination);

    }

    @Override
    public Object setDestinationZpk(HashMap<String, String> request) {
        log.info("about injecting destination ZPK");
        Destination destination = destinationRepository.findByDestinationName(request.get("destination")).orElseThrow(() -> {
            log.error("destination not found");
            return new OmniproApplicationException(MessageCode.NOT_FOUND, "destination not found");
        });
        destination.setZpk(request.get("zpk").substring(0, 32));
        if (request.get("zmk") != null) {
            destination.setZpk(request.get("zmk").substring(0, 32));
        }
        return destinationRepository.save(destination);

    }


    private Pair<String, String> extractMasterSessionKeyData(ISOMsg isoMsg) throws KeyExchangeException, CryptoException {
        String fullKeyData;
        if (isoMsg.hasField(125)) {
            fullKeyData = isoMsg.getString(125);

        } else {
            if (!isoMsg.hasField(53)) {
                throw new KeyExchangeException("Error extracting key data, no key data found in both field 53 and field 125");
            }
            fullKeyData = isoMsg.getString(53);

        }
        final String sessionKey = fullKeyData.substring(0, 32);
        final String kcv = fullKeyData.substring(32, 38);
        return new ImmutablePair<>(sessionKey, kcv);
    }


    public List<InstitutionDestination> getDestinationsByInstitution(String institutionId) {
        return institutionDestinationRepository.findByInstitutionId(institutionId);
    }


    public DestinationResponse attachDestinationToInstitution(String institutionId, String destinationName) {
        if (!institutionRepository.existsByInstitutionId(institutionId)) {
            throw new OmniproApplicationException(MessageCode.NOT_FOUND, "institution");
        }

        Destination destination = destinationRepository.findByDestinationName(destinationName)
                .orElseThrow(() -> new OmniproApplicationException(MessageCode.NOT_FOUND, "destination"));

        // Check if InstitutionDestination already exists
        boolean exists = institutionDestinationRepository.existsByInstitutionIdAndDestination(institutionId, destination);
        if (exists) {
            throw new OmniproApplicationException(MessageCode.DUPLICATE_ENTITY, "InstitutionDestination already exists");
        }

        InstitutionDestination institutionDestination = InstitutionDestination.builder()
                .institutionId(institutionId)
                .destination(destination)
                .build();

        institutionDestination = institutionDestinationRepository.save(institutionDestination);

        return new DestinationResponse(institutionDestination.getDestination());
    }


}
