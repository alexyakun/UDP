package udp.Behaviours;


import jade.core.behaviours.OneShotBehaviour;
import udp.AgentDetector;


public class ClientBeh extends OneShotBehaviour {
    @Override
    public void action() {
        AgentDetector agentDetector = new AgentDetector();
        agentDetector.startPublishing(getAgent().getAID(),1200);

    }
}
