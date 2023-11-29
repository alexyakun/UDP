package udp;

import jade.core.Agent;
import lombok.extern.slf4j.Slf4j;
import udp.Behaviours.ClientBeh;
import udp.Behaviours.ServerBeh;


@Slf4j
public class MyAgent extends Agent {
    @Override
    protected void setup() {
        log.info("{} was born", getLocalName());
        if(getLocalName().equals("agent1")) {
            addBehaviour(new ServerBeh());
        }
        addBehaviour(new ClientBeh());
    }
}
