How to Run Vero Switch
Vero Switch is a middleware that facilitates card processing transactions. To run this project, follow the steps below. This includes building the Docker container, configuring the server, and sending ISO 8583 messages via TCP/IP (JPOS).

Prerequisites
Docker: Ensure you have Docker installed on your machine to build and run the container.

JPOS: If you're familiar with ISO 8583-based messaging protocols (like JPOS), you will use this to send messages to the server.

Network Configuration: Make sure you have TCP/IP access to the server to send ISO messages.

Step 1: Clone the Repository
Clone the Vero Switch repository to your local machine:

bash
Copy
git clone https://github.com/payus123/vero-switch.git
cd vero-switch
Step 2: Build the Docker Container
To build the Docker container for the project, run the following command in the project's root directory (where the Dockerfile is located):

bash
Copy
docker build -t vero-switch .
This will create a Docker image named vero-switch.

Step 3: Run the Docker Container
Once the image is built, you can run the container with:

bash
Copy
docker run -d -p 5335:5335 vero-switch
This will start the container in detached mode, mapping port 5335 on the host to port 5335 on the container (you can modify the ports as needed).

Step 4: Configure TCP/IP for ISO 8583 Messages
The server is now set up to receive ISO 8583 messages via TCP/IP. You will need to configure your JPOS client or any other ISO 8583 message-sending tool to point to the running server.

Server Host: Use localhost or the IP address of the machine running the container.

Port: Use the port you mapped for the container, by default it’s 5335.

Step 5: Send ISO Messages
With the server running, you can now send ISO 8583 messages via TCP/IP. Here's an example of how you can set this up with a JPOS client:

Open your JPOS client configuration and set the destination IP address and port to the server's IP and port (for example, localhost:8000).

Send a transaction request message (ISO 8583 message) to the server.

You should receive a response from the server (depending on your transaction logic and setup).

Step 6: Monitor the Server
To view logs and ensure everything is running smoothly, you can monitor the Docker container logs:

bash
Copy
docker logs -f <container_id>
Troubleshooting
Connection Issues: Ensure that your firewall or network settings are allowing traffic on the port you're using.

Message Format: Ensure that the ISO 8583 message you're sending is properly formatted according to the project's expected format.

Additional Configuration
If you need to make any changes to the server configuration, you can do so by modifying the relevant files in the repository. Once you make the changes, rebuild the Docker container with:

bash
Copy
docker build -t vero-switch .
Then, restart the container:

bash
Copy
docker restart <container_id>
Conclusion
With these steps, you should be able to run the Vero Switch middleware for card transaction processing. If you encounter any issues, review the logs for error messages or consult the documentation for additional configuration options.

