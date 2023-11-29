package udp;

import jade.core.ProfileImpl;
import jade.util.ExtendedProperties;
import jade.core.Runtime;
import java.util.List;
import java.util.Map;

public class JadeStarter {
    public static void main(String[] args) {
        Map<String, Class<?>> agents = Map.of(
                "agent1", MyAgent.class,
                "agent2", MyAgent.class,
                "agent3", MyAgent.class
        );

        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("gui", "true");
        props.setProperty("agents", addAgents(agents));
//        props.setProperty("services", addServices(List.of("jade.core.messaging.TopicManagementService")));
        ProfileImpl p = new ProfileImpl(props);

        Runtime.instance().setCloseVM(true);
        Runtime.instance().createMainContainer(p);
    }

    private static String addAgents(Map<String, Class<?>> createAgents){
        StringBuilder outString = new StringBuilder();
        for (Map.Entry<String, Class<?>> entry : createAgents.entrySet()) {
            outString.append(entry.getKey()).append(":").append(entry.getValue().getName()).append(";");
        }
        System.out.println(outString);
        return outString.toString();
    }

    private static String addServices(List<String> services){
        StringBuilder outString = new StringBuilder();
        for (String service : services) {
            outString.append(service).append(";");
        }
        return outString.toString();
    }
}

