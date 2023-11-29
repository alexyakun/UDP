package udp;

import jade.core.Agent;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class MyAgent extends Agent {
    @Override
    protected void setup() {
        log.info("{} was born", getLocalName());
        AgentDetector agentDetector =new AgentDetector();
        agentDetector.publishActiveAgent(this);
        agentDetector.startDiscovering(1200, getAID());
        agentDetector.startPublishing(this, 1200);

    }
}
