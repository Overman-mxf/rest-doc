package restdoc.remoting.common.body;

import restdoc.remoting.common.ExposedAPI;
import restdoc.remoting.protocol.RemotingSerializable;

import java.util.List;

public abstract class BaseExposedAPIBody extends RemotingSerializable {

    /**
     * @return api list
     */
    public abstract List<? extends ExposedAPI> getApiList();

    /**
     * Application service name
     */
    public String service;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
