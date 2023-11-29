package udp;

import jade.core.AID;
import lombok.SneakyThrows;
import udp.dto.AgentDTO;
import udp.utils.JsonUtils;
import udp.utils.PacketCreator;


import java.util.List;

public class AgentDetector {

    @SneakyThrows
    public void startPublishing(AID aid, int port){
        String name = aid.getLocalName();
        boolean isGuid = true;
        AgentDTO agentDTO = new AgentDTO(name, isGuid);
        String code = JsonUtils.code(agentDTO);
        byte[] bytes = PacketCreator.create(code);
        RawUdpSocketClient client = new RawUdpSocketClient();
        client.initialize(port);
        for (byte aByte : bytes) {
            System.out.print(aByte+" ");
        }
        System.out.println();

        while (true){
            client.send(bytes);
            Thread.sleep(2000);
        }

    }

    public void startDiscovering(int port, AID aid){
        RawUdpSocketServer rawServer = new RawUdpSocketServer(aid);
        rawServer.start(port);
    }
    public List<AID> getActiveAgents(){
        return null;
    }
}
