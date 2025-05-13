package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.destinations.model.*;

public interface ChannelService {
    boolean createChannelMuxConfig(Destination destination);
    boolean updateChannelMuxConfig(Destination destination);
}
