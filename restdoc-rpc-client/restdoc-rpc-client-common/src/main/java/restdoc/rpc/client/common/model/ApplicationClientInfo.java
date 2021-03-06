package restdoc.rpc.client.common.model;

import io.netty.channel.Channel;
import restdoc.remoting.protocol.LanguageCode;

import java.net.InetSocketAddress;
import java.util.Objects;


/**
 * ApplicationClientInfo
 */
public class ApplicationClientInfo {
    private final Channel channel;
    private final LanguageCode language;
    private final int version;
    private volatile long lastUpdateTimestamp = System.currentTimeMillis();
    private String hostname;
    private String osname;
    private String service;
    private ApplicationType applicationType = ApplicationType.REST_WEB;
    private String serializationProtocol;

    public ApplicationClientInfo(  Channel channel, LanguageCode language, int version) {
        this.channel = channel;
        this.language = language;
        this.version = version;

        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        this.hostname = address.getHostName();
    }

    public Channel getChannel() {
        return channel;
    }

    public LanguageCode getLanguage() {
        return language;
    }

    public int getVersion() {
        return version;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOsname() {
        return osname;
    }

    public void setOsname(String osname) {
        this.osname = osname;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSerializationProtocol() {
        return serializationProtocol;
    }

    public void setSerializationProtocol(String serializationProtocol) {
        this.serializationProtocol = serializationProtocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationClientInfo that = (ApplicationClientInfo) o;
        return version == that.version &&
                lastUpdateTimestamp == that.lastUpdateTimestamp &&
                Objects.equals(channel, that.channel) &&
                language == that.language &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(osname, that.osname) &&
                Objects.equals(service, that.service) &&
                applicationType == that.applicationType &&
                Objects.equals(serializationProtocol, that.serializationProtocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, language, version, lastUpdateTimestamp, hostname, osname, service, applicationType, serializationProtocol);
    }

    @Override
    public String toString() {
        return "ApplicationClientInfo{" +
                "channel=" + channel +
                ", language=" + language +
                ", version=" + version +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", hostname='" + hostname + '\'' +
                ", osname='" + osname + '\'' +
                ", service='" + service + '\'' +
                ", applicationType=" + applicationType +
                ", serializationProtocol='" + serializationProtocol + '\'' +
                '}';
    }
}
