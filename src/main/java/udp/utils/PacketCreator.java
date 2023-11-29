package udp.utils;

import java.util.ArrayList;
import java.util.List;

public class PacketCreator {
    public static byte[] create(String data){
        int len = data.getBytes().length+20+8;
        byte len1 = (byte) (len >> 8 & 255);
        byte len2 = (byte)(len & 255);

        List<Byte> bytes = new ArrayList<>();
        byte[] start = {2,0,0,0,69,0,len1,len2,(byte)195,(byte) 164, 0, 0,(byte) 128, 17, 0, 0,
                (byte) 172, (byte)20, (byte) 10, (byte) 9,
                (byte) 172, (byte)20, (byte) 10, (byte) 9};
        for (byte b : start) {
            bytes.add(b);
        }
        int dPort = 56878;
        byte dPortb1 = (byte)(dPort >> 8 & 255);
        bytes.add(dPortb1);
        byte dPortb2 = (byte)(dPort & 255);
        bytes.add(dPortb2);
        int sPort = 1200;
        byte sPortb1 = (byte) (sPort >> 8 & 255);
        bytes.add(sPortb1);
        byte sPortb2 = (byte)(sPort & 255);
        bytes.add(sPortb2);

        int length = data.getBytes().length+8;
        byte length1 = (byte) (length >> 8 & 255);
        bytes.add(length1);

        byte length2 = (byte)(length & 255);
        bytes.add(length2);

        bytes.add((byte)0);
        bytes.add((byte)0);

        for (byte aByte : data.getBytes()) {
            bytes.add(aByte);
        }

        byte[] massByte = new byte[bytes.size()];
        int i = 0;
        for (Byte aByte : bytes) {
            massByte[i] = aByte;
            i++;
        }

// Формирование массива для подсчета контрольной суммы
        byte [] udpData = new byte[massByte.length-24+12];
        for (int j = 12; j < udpData.length; j++) {
            udpData[j]= massByte[j+12];
        }
        //  Формирование псевдозаголовка
        udpData[0]=massByte[16];
        udpData[1]=massByte[17];
        udpData[2]=massByte[18];
        udpData[3]=massByte[19];
        udpData[4]=massByte[20];
        udpData[5]=massByte[21];
        udpData[6]=massByte[22];
        udpData[7]=massByte[23];
        udpData[8]=(byte) 0;
        udpData[9]=17;
        udpData[10]=massByte[28];
        udpData[11]=massByte[29];

//                System.out.println(Arrays.toString(rawData));

        System.out.println(bytes);
        int checksum = CheckSum.calculate(udpData);
        byte cSum1 = (byte) (checksum>>8&0xFF);
        byte cSum2 = (byte) (checksum&0xFF);
        System.out.print("udp data");
        for (byte udpDatum : udpData) {
            System.out.print(udpDatum+" ");
        }
        System.out.println();
        System.out.print("udp massByte");
        for (byte udpDatum : massByte) {
            System.out.print(udpDatum+" ");
        }
        System.out.println();
        massByte[30]= (byte) (cSum1-1);
        massByte[31] = cSum2;
        return massByte;
    }

}
