package udp;

import jade.core.AID;
import jade.core.Agent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import udp.dto.AgentDTO;
import udp.utils.JsonUtils;
import udp.utils.PacketCreator;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AgentDetector {
    private RawUdpSocketServer rawServer;
    private boolean run = true;

    @SneakyThrows
    public void startPublishing(Agent agent, int port){
        AID aid = agent.getAID();
        String name = aid.getLocalName();
        boolean isGuid = AID.ISGUID;
        AgentDTO agentDTO = new AgentDTO(name, isGuid);
        String code = JsonUtils.code(agentDTO);
        byte[] bytes = PacketCreator.create(code);
        RawUdpSocketClient client = new RawUdpSocketClient();
        client.initialize(port);
        for (byte aByte : bytes) {
            System.out.print(aByte+" ");
        }
        System.out.println();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(()->{
            if(!agent.isAlive()){
                stopDiscovering();
                run = false;
                service.shutdown();
            }
            client.send(bytes);
        },3000,3000, TimeUnit.MILLISECONDS);
    }

    public void startDiscovering(int port, AID aid){
        rawServer = new RawUdpSocketServer(aid);
        rawServer.start(port);

    }
    public void stopDiscovering(){
        rawServer.setRun(false);
    }
    public void publishActiveAgent(Agent agent){
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(()->{
            if(!run){
                service.shutdown();
            }
            List<AID> activeAgents = getActiveAgents(agent.getAID());
            List<String> nameList = activeAgents.stream().map(AID::getLocalName).toList();
            log.info("{} have this ALIVE LIST: {}",agent.getLocalName(),nameList);
        },3000,3000, TimeUnit.MILLISECONDS);

    }
    public List<AID> getActiveAgents(AID aid){
        Map<String, Long> activeAgent = rawServer.getActiveAgent();
        List<AID> actAgent = new ArrayList<>();
        for (Map.Entry<String, Long> entry : activeAgent.entrySet()) {
            String msg = entry.getKey();
            AgentDTO agentDTO = JsonUtils.decode(msg, AgentDTO.class).get();
            actAgent.add(new AID(agentDTO.getName(), agentDTO.isGuid()));
        }

        return actAgent;
    }
}
