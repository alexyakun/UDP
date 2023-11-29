package udp;

import com.sun.jna.NativeLibrary;
import jade.core.AID;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.*;
import udp.dto.AgentDTO;
import udp.utils.JsonUtils;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class RawUdpSocketServer {

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")){
            NativeLibrary.addSearchPath("wpcap","C:\\Windows\\System32\\Npcap");
        }
    }
    protected boolean run;
    private AID aid;
    private Map<String, Long> activeAgent = new ConcurrentHashMap<>();


    public RawUdpSocketServer(AID aid){
        this.aid = aid;
    }

    @SneakyThrows
    public void start(int port){
        run = true;
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
        PcapNetworkInterface networkInterface = null;
        for (PcapNetworkInterface allDev : allDevs) {
            if(allDev.getName().equals("\\Device\\NPF_Loopback")){
                networkInterface = allDev;
                break;
            }
        }
        PcapHandle pcapHandle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 50);
        pcapHandle.setFilter("ip proto \\udp && dst port "+port, BpfProgram.BpfCompileMode.NONOPTIMIZE);

        runInTread(pcapHandle);
    }

    protected void runInTread(PcapHandle pcapHandle){
        new Thread(() -> {
            grabPackets(pcapHandle);
        }).start();
    }

    protected void grabPackets(PcapHandle pcapHandle){
        try{
            pcapHandle.loop(0, (PacketListener) packet -> {
                byte [] rawData = packet.getRawData();
                byte [] data = new byte[rawData.length-32];
//                System.out.println(Arrays.toString(rawData));
                System.arraycopy(rawData, 32, data, 0, data.length);
                String message = new String(data);
                long l = System.currentTimeMillis();
                Optional<AgentDTO> decode = JsonUtils.decode(message, AgentDTO.class);
                if(decode.isPresent()){
                    AgentDTO agentDTO = decode.get();
                    String name = agentDTO.getName();

                    if(!name.equals(aid.getLocalName())) {
                        activeAgent.put(message,System.currentTimeMillis());

                        log.info("{} receive message {} from {}", aid.getLocalName(),message,name);
                    }
                    for (Map.Entry<String, Long> entry : activeAgent.entrySet()) {
                        if(System.currentTimeMillis()-entry.getValue()>4000){
                            String nameOfAgent = JsonUtils.decode(entry.getKey(), AgentDTO.class).get().getName();
                            log.error("{} REMOVE {} from his active LIST",aid.getLocalName(),nameOfAgent);
                            activeAgent.remove(entry.getKey());
                        }
                    }
                }
                if(!run){
                    try{
                        pcapHandle.breakLoop();
                    } catch (NotOpenException e){
                        throw new RuntimeException(e);
                    }

                }
            });
        } catch (PcapNativeException | NotOpenException e){
            throw new RuntimeException(e);

        }
        catch (InterruptedException e){
            log.error("{} STOP RAWUDPServer",aid.getLocalName());
        }
    }
}
