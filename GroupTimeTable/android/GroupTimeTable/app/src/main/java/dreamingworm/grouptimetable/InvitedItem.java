package dreamingworm.grouptimetable;

import java.io.Serializable;

/**
 * Created by sungwoo on 2016-08-01.
 */

public class InvitedItem implements Serializable {
    private String roomName;
    private String hostName;
    private String groupSN;
    private String hostNickname;
    private int groupImg;

    public InvitedItem(String groupSN, String hostName, String hostNickname, String roomName,int groupImg) {
        this.groupSN = groupSN;
        this.hostName = hostName;
        this.hostNickname = hostNickname;
        this.roomName = roomName;
        this.groupImg=groupImg;
    }

    public int getGroupImg() {
        return groupImg;
    }

    public void setGroupImg(int groupImg) {
        this.groupImg = groupImg;
    }

    public String getGroupSN() {
        return groupSN;
    }

    public void setGroupSN(String groupSN) {
        this.groupSN = groupSN;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostNickname() {
        return hostNickname;
    }

    public void setHostNickname(String hostNickname) {
        this.hostNickname = hostNickname;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}