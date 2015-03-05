package nl.senseos.mytimeatsense.bluetooth;

/**
 * Created by ronald on 4-3-15.
 */
public class Label {

    private long localId = -1;
    private long remoteId = -1;
    private String name;
    private String description;

    public Label(String name, String description){

        this.name = name;
        this.description = description;

    }

    public Label(String name, String description, int localId){

        this.name = name;
        this.description = description;
        this.localId = localId;

    }

    public Label(String name, String description, int localId, int remoteId){

        this.name = name;
        this.description = description;
        this.localId = localId;
        this.remoteId = remoteId;

    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public long getLocalId(){
        return localId;
    }

    public long getRemoteId(){
        return remoteId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRemoteId(long remoteId) {
        this.remoteId = remoteId;
    }


}
