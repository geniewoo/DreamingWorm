package dreamingworm.grouptimetable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Youngs on 2016-07-15.
 */
public class AddFriendListItem {
    private Bitmap icon;
    private String name;
    private String nickname;

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Bitmap getIcon() {
        return this.icon;
    }

    public String getName() {
        return this.name;
    }

    public String getNickname() {
        return this.nickname;
    }


}
