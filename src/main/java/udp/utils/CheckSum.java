package udp.utils;

public class CheckSum {
    public static int calculate(byte[] bytes){
        int checksum=0;
        for(int i = 0 ; i < bytes.length-1; i +=2 ){
            checksum += ((bytes[i]*256)+(bytes[i+1]))&0xFFFF;
        }
        if(bytes.length%2!=0){
            checksum+=(bytes[bytes.length-1]*256)&0xFFFF;
        }
        while(checksum>>16>0) {
            checksum = (checksum >> 16) + (checksum& 0xFFFF);
        }
        return (~checksum);
    }
}
