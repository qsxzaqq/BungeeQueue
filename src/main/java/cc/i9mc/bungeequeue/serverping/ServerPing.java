package cc.i9mc.bungeequeue.serverping;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerPing {
    private int maxplayers = 0;
    private int players = 0;
    private String description = "";
    private boolean end;

    public void ping(InetSocketAddress Address, int timeout) {
        end = false;
        try {
            ServerConnection connection = new ServerConnection(Address, timeout);
            connection.connect();
            QueryHandler queryHandler = new QueryHandler(connection);
            queryHandler.doHandShake();
            StatusResponse response = queryHandler.doStatusQuery();
            connection.disconnect();

            maxplayers = response.getPlayers().getMax();
            players = response.getPlayers().getOnline();
            description = response.getDescription();
        } catch (IOException ignored) {
            end = true;
        }
    }

    public int getMaxplayers() {
        return maxplayers;
    }

    public int getPlayers() {
        return players;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnd() {
        return end;
    }
}

class ServerConnection {
    private final InetSocketAddress host;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int timeout;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    ServerConnection(InetSocketAddress host, int timeout) {
        this.timeout = timeout;
        this.host = host;
    }

    void connect() throws IOException {
        this.socket = new Socket();
        this.socket.setSoTimeout(this.timeout);
        this.socket.connect(this.host, this.timeout);
        this.inputStream = this.socket.getInputStream();
        this.dataInputStream = new DataInputStream(this.inputStream);
        this.outputStream = this.socket.getOutputStream();
        this.dataOutputStream = new DataOutputStream(this.outputStream);
    }

    void disconnect() throws IOException {
        this.dataInputStream.close();
        this.dataOutputStream.close();
        this.socket.close();
    }

    InetSocketAddress getHost() {
        return this.host;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public int getTimeout() {
        return this.timeout;
    }

    DataInputStream getDataInputStream() {
        return this.dataInputStream;
    }

    DataOutputStream getDataOutputStream() {
        return this.dataOutputStream;
    }
}

class QueryHandler {
    private static final Gson gson = new Gson();
    private final ServerConnection connection;

    QueryHandler(ServerConnection connection) {
        this.connection = connection;
    }

    void doHandShake() throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bs);
        out.write(0);
        this.writeVarInt(out, 4);
        this.writeString(out, this.connection.getHost().getHostString());
        out.writeShort(this.connection.getHost().getPort());
        this.writeVarInt(out, 1);
        this.sendPacket(bs.toByteArray());
    }

    StatusResponse doStatusQuery() throws IOException {
        this.sendPacket(new byte[1]);
        this.readVarInt(this.connection.getDataInputStream());
        int packetId = this.readVarInt(this.connection.getDataInputStream());
        if (packetId != 0) {
            throw new IOException("Invalid packetId");
        } else {
            int stringLength = this.readVarInt(this.connection.getDataInputStream());
            if (stringLength < 1) {
                throw new IOException("Invalid string length.");
            } else {
                byte[] responseData = new byte[stringLength];
                this.connection.getDataInputStream().readFully(responseData);
                String jsonString = new String(responseData, StandardCharsets.UTF_8);
                return gson.fromJson(jsonString, StatusResponse.class);
            }
        }
    }

    private void sendPacket(byte[] data) throws IOException {
        this.writeVarInt(this.connection.getDataOutputStream(), data.length);
        this.connection.getDataOutputStream().write(data);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;

        byte k;
        do {
            k = in.readByte();
            i |= (k & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((k & 128) == 128);

        return i;
    }

    private void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while ((paramInt & -128) != 0) {
            out.write(paramInt & 127 | 128);
            paramInt >>>= 7;
        }

        out.write(paramInt);
    }

    private void writeString(DataOutputStream out, String string) throws IOException {
        this.writeVarInt(out, string.length());
        out.write(string.getBytes(StandardCharsets.UTF_8));
    }
}

class StatusResponse {
    private Object description;
    private Players players;
    private Version version;
    private String favicon;
    private int time;

    public String getDescription() {
        if(this.description instanceof String){
            return (String) description;
        }

        return (String) ((LinkedTreeMap) this.description).get("text");
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Players getPlayers() {
        return this.players;
    }

    public void setPlayers(Players players) {
        this.players = players;
    }

    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getFavicon() {
        return this.favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof StatusResponse)) {
            return false;
        } else {
            StatusResponse other = (StatusResponse) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label63:
                {
                    Object this$description = this.getDescription();
                    Object other$description = other.getDescription();
                    if (this$description == null) {
                        if (other$description == null) {
                            break label63;
                        }
                    } else if (this$description.equals(other$description)) {
                        break label63;
                    }

                    return false;
                }

                Object this$players = this.getPlayers();
                Object other$players = other.getPlayers();
                if (this$players == null) {
                    if (other$players != null) {
                        return false;
                    }
                } else if (!this$players.equals(other$players)) {
                    return false;
                }

                Object this$version = this.getVersion();
                Object other$version = other.getVersion();
                if (this$version == null) {
                    if (other$version != null) {
                        return false;
                    }
                } else if (!this$version.equals(other$version)) {
                    return false;
                }

                label42:
                {
                    Object this$favicon = this.getFavicon();
                    Object other$favicon = other.getFavicon();
                    if (this$favicon == null) {
                        if (other$favicon == null) {
                            break label42;
                        }
                    } else if (this$favicon.equals(other$favicon)) {
                        break label42;
                    }

                    return false;
                }

                if (this.getTime() == other.getTime()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof StatusResponse;
    }

    public int hashCode() {
        int result = 1;
        Object $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        Object $players = this.getPlayers();
        result = result * 59 + ($players == null ? 43 : $players.hashCode());
        Object $version = this.getVersion();
        result = result * 59 + ($version == null ? 43 : $version.hashCode());
        Object $favicon = this.getFavicon();
        result = result * 59 + ($favicon == null ? 43 : $favicon.hashCode());
        result = result * 59 + this.getTime();
        return result;
    }

    public String toString() {
        return "StatusResponse(description=" + this.getDescription() + ", players=" + this.getPlayers() + ", version=" + this.getVersion() + ", favicon=" + this.getFavicon() + ", time=" + this.getTime() + ")";
    }

    public class Player {
        private String name;
        private String id;

        public Player() {
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Player)) {
                return false;
            } else {
                Player other = (Player) o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label31:
                    {
                        Object this$name = this.getName();
                        Object other$name = other.getName();
                        if (this$name == null) {
                            if (other$name == null) {
                                break label31;
                            }
                        } else if (this$name.equals(other$name)) {
                            break label31;
                        }

                        return false;
                    }

                    Object this$id = this.getId();
                    Object other$id = other.getId();
                    return this$id == null ? other$id == null : this$id.equals(other$id);
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Player;
        }

        public int hashCode() {
            int result = 1;
            Object $name = this.getName();
            int resultx = result * 59 + ($name == null ? 43 : $name.hashCode());
            Object $id = this.getId();
            resultx = resultx * 59 + ($id == null ? 43 : $id.hashCode());
            return resultx;
        }

        public String toString() {
            return "StatusResponse.Player(name=" + this.getName() + ", id=" + this.getId() + ")";
        }
    }

    class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public Players() {
        }

        public int getMax() {
            return this.max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getOnline() {
            return this.online;
        }

        public void setOnline(int online) {
            this.online = online;
        }

        public List<Player> getSample() {
            return this.sample;
        }

        public void setSample(List<Player> sample) {
            this.sample = sample;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Players)) {
                return false;
            } else {
                Players other = (Players) o;
                if (!other.canEqual(this)) {
                    return false;
                } else if (this.getMax() != other.getMax()) {
                    return false;
                } else if (this.getOnline() != other.getOnline()) {
                    return false;
                } else {
                    Object this$sample = this.getSample();
                    Object other$sample = other.getSample();
                    return this$sample == null ? other$sample == null : this$sample.equals(other$sample);
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Players;
        }

        public int hashCode() {
            int resultx = 1;
            int result = resultx * 59 + this.getMax();
            result = result * 59 + this.getOnline();
            Object $sample = this.getSample();
            result = result * 59 + ($sample == null ? 43 : $sample.hashCode());
            return result;
        }

        public String toString() {
            return "StatusResponse.Players(max=" + this.getMax() + ", online=" + this.getOnline() + ", sample=" + this.getSample() + ")";
        }
    }

    public class Version {
        private String name;
        private int protocol;

        public Version() {
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getProtocol() {
            return this.protocol;
        }

        public void setProtocol(int protocol) {
            this.protocol = protocol;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Version)) {
                return false;
            } else {
                Version other = (Version) o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$name = this.getName();
                    Object other$name = other.getName();
                    if (this$name == null) {
                        if (other$name == null) {
                            return this.getProtocol() == other.getProtocol();
                        }
                    } else if (this$name.equals(other$name)) {
                        return this.getProtocol() == other.getProtocol();
                    }

                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Version;
        }

        public int hashCode() {
            boolean PRIME = true;
            int resultx = 1;
            Object $name = this.getName();
            int result = resultx * 59 + ($name == null ? 43 : $name.hashCode());
            result = result * 59 + this.getProtocol();
            return result;
        }

        public String toString() {
            return "StatusResponse.Version(name=" + this.getName() + ", protocol=" + this.getProtocol() + ")";
        }
    }
}
