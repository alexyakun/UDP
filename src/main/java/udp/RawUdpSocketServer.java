package udp;

import com.sun.jna.NativeLibrary;
import jade.core.AID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.*;
import udp.dto.AgentDTO;
import udp.utils.JsonUtils;


import java.util.List;
import java.util.Optional;

@Slf4j
public class RawUdpSocketServer {

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")){
            NativeLibrary.addSearchPath("wpcap","C:\\Windows\\System32\\Npcap");
        }
    }
    private AID aid;
    public RawUdpSocketServer(AID aid){
        this.aid = aid;
    }

    protected boolean run = true;

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
                Optional<AgentDTO> decode = JsonUtils.decode(message, AgentDTO.class);
                if(decode.isPresent()){
                    AgentDTO agentDTO = decode.get();
                    String name = agentDTO.getName();
                    if(!name.equals(aid.getLocalName())) {
                        log.info("{} receive message {} from {}", aid.getLocalName(),message,name);
                    }
                }
                if(!run){
                    try{
                        pcapHandle.breakLoop();
                    } catch (NotOpenException e){
                        e.printStackTrace();
                    }
                }
            });
        } catch (PcapNativeException | InterruptedException | NotOpenException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
