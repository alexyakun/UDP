package udp.Behaviours;

import jade.core.behaviours.OneShotBehaviour;
import udp.AgentDetector;


public class ServerBeh extends OneShotBehaviour {
    @Override
    public void action() {
        AgentDetector agentDetector = new AgentDetector();
        agentDetector.startDiscovering(1200,getAgent().getAID());
    }
}
