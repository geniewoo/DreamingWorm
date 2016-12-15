package dreamingworm.grouptimetable;

import android.graphics.Bitmap;

/**
 * Created by Youngs on 2016-08-03.
 */
public class GroupTableListItem {
    private Bitmap icon;
    private String name;
    private String nickname;

    public char getFlag() {
        return flag;
    }

    public void setFlag(char flag) {
        this.flag = flag;
    }

    private char flag;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
}

