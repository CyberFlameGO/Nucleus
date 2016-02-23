/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.config.serialisers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.MuteData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public class UserConfig {
    @Setting
    private MuteData muteData;

    @Setting
    private boolean socialspy;

    @Setting("lastLogin")
    private long login;

    @Setting("lastLogout")
    private long logout;

    @Setting
    private boolean invulnerable;

    @Setting
    private boolean fly;

    @Setting("mail")
    private List<MailData> mailDataList = Lists.newArrayList();

    @Setting
    private JailData jailData;

    @Setting
    private LocationNode locationOnLogin;

    @Setting(value = "jailOnNextLogin")
    private boolean jailOffline = false;

    @Setting("homes")
    private Map<String, LocationNode> homeData = Maps.newHashMap();

    @Setting("tptoggle")
    private boolean isTeleportToggled = true;

    @Setting
    private String nickname;

    public MuteData getMuteData() {
        return muteData;
    }

    public void setMuteData(MuteData muteData) {
        this.muteData = muteData;
    }

    public boolean isSocialspy() {
        return socialspy;
    }

    public void setSocialspy(boolean socialspy) {
        this.socialspy = socialspy;
    }

    public long getLogin() {
        return login;
    }

    public void setLogin(long login) {
        this.login = login;
    }

    public long getLogout() {
        return logout;
    }

    public void setLogout(long logout) {
        this.logout = logout;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public boolean isFly() {
        return fly;
    }

    public void setFly(boolean fly) {
        this.fly = fly;
    }

    public List<MailData> getMailDataList() {
        return mailDataList;
    }

    public void setMailDataList(List<MailData> mailDataList) {
        this.mailDataList = mailDataList;
    }

    public JailData getJailData() {
        return jailData;
    }

    public void setJailData(JailData jailData) {
        this.jailData = jailData;
    }

    public LocationNode getLocationOnLogin() {
        return locationOnLogin;
    }

    public void setLocationOnLogin(LocationNode locationOnLogin) {
        this.locationOnLogin = locationOnLogin;
    }

    public boolean isJailOffline() {
        return jailOffline;
    }

    public void setJailOffline(boolean jailOffline) {
        this.jailOffline = jailOffline;
    }

    public Map<String, LocationNode> getHomeData() {
        return homeData;
    }

    public void setHomeData(Map<String, LocationNode> homeData) {
        this.homeData = homeData;
    }

    public boolean isTeleportToggled() {
        return isTeleportToggled;
    }

    public void setTeleportToggled(boolean teleportToggled) {
        isTeleportToggled = teleportToggled;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
