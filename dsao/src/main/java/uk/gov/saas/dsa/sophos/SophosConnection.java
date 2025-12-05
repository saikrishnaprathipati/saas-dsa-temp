package uk.gov.saas.dsa.sophos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class SophosConnection {
    private String ipOrName;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected;
    private String fileName;
    private String fileNameAndPath;
    private int fileSize;
    private byte[] ba;
    private static Logger logger;
    private String connectionId;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:sss");
    private String transactionString;

    public SophosConnection(String ipOrNameIn, int portIn) {
        logger = Logger.getLogger("uk.gov.saas.advisorupload");
        this.socket = null;
        this.ipOrName = ipOrNameIn;
        this.port = portIn;
        this.out = null;
        this.in = null;
        this.fileName = "";
        this.fileSize = 0;
        this.ba = null;
        this.connected = this.connect();
    }

    private boolean connect() {
        boolean response = false;

        try {
            this.socket = new Socket();
            this.socket.setKeepAlive(true);
            InetSocketAddress isa = new InetSocketAddress(this.ipOrName, this.port);
            this.socket.connect(isa, 5000);
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String reply = this.readSingleEOLReply();
            response = this.checkResponse(reply, true, false);
            if (!response) {
                return false;
            } else {
                this.out.write("SSSP/1.0 OPTIONS\n");
                this.out.write("report: virus\n");
                this.out.write("report: error\n\n");
                this.out.flush();
                reply = this.readDoubleEOLReply();
                this.setConnectionId(reply.substring(4, 12));
                response = this.checkResponse(reply, false, false);
                return response;
            }
        } catch (Exception var4) {
            logger.info("SophosConnection : connect() : Error trying to connect to server " + this.ipOrName + " : " + var4.toString());
            return false;
        }
    }

    public boolean checkConnect() {
        new Date();

        try {
            return this.socket.isConnected();
        } catch (Exception var4) {
            logger.info("SophosConnection : checkConnect() : Error trying to check connection to server " + this.ipOrName + " : " + var4.toString());
            return false;
        }
    }

    public void disconnect() {
        try {
            this.out.write("SSSP/1.0 BYE\n");
            this.out.flush();
            String reply = this.readDoubleEOLReply();
            if (this.out != null) {
                this.out.close();
                this.out = null;
            }

            if (this.in != null) {
                this.in.close();
                this.in = null;
            }

            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception var2) {
            logger.info("SophosConnection : disconnect() : Error trying to disconnect from " + this.ipOrName + " : " + var2.toString());
        }

    }

    public boolean virusCheckFile(String filenameandpathIn, String transactionString, boolean debug) throws Exception {
        this.transactionString = transactionString;
        boolean cleanImage = false;

        try {
            String reply = null;
            this.fileNameAndPath = filenameandpathIn;
            this.out.write("SSSP/1.0 SCANFILE " + this.fileNameAndPath + "\n");
            this.out.flush();
            reply = this.readDoubleEOLReply();
            cleanImage = this.checkResponse(reply, false, false);
            return cleanImage;
        } catch (Exception var6) {
            throw new Exception("SophosConnection : virusCheck() : Error performing virus check : " + var6.toString());
        }
    }

    public boolean virusCheckStream(String fileNameIn, byte[] baIn, int fileSizeIn, String transactionString, boolean debug) throws Exception {
        this.transactionString = transactionString;
        boolean cleanImage = false;

        try {
            String reply = null;
            this.fileName = fileNameIn;
            this.ba = baIn;
            this.fileSize = fileSizeIn;
            byte[] fileContent = new byte[this.fileSize];
            String strFileContent = new String(this.ba);
            this.out.write("SSSP/1.0 SCANDATA " + this.fileSize + "\n");
            this.out.write(strFileContent + "\n\n");
            this.out.flush();
            reply = this.readDoubleEOLReply();
            cleanImage = this.checkResponse(reply, false, false);
            return cleanImage;
        } catch (Exception var10) {
            throw new Exception("SophosConnection : virusCheck() : Error performing virus check : " + var10.toString());
        }
    }

    private String readSingleEOLReply() throws Exception {
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            boolean continueToRead = true;
            boolean tenRcvd = false;
            boolean thirteenRcvd = false;

            while(continueToRead) {
                int aa = this.in.read();
                if (aa == -1) {
                    continueToRead = false;
                    break;
                }

                if (aa != 13 && aa != 10) {
                    sb.append((char)aa);
                } else if (aa == 13) {
                    thirteenRcvd = true;
                } else if (aa == 10) {
                    tenRcvd = true;
                }

                if (tenRcvd && thirteenRcvd) {
                    continueToRead = false;
                }
            }
        } catch (Exception var6) {
            throw new Exception("SophosConnection : readSingleEOLReply() : Error reading reply : " + var6.toString());
        }

        return sb.toString();
    }

    private String readDoubleEOLReply() throws Exception {
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            boolean continueToRead = true;
            boolean tenRcvd = false;
            boolean thirteenRcvd = false;
            boolean eolRcvd = false;

            while(continueToRead) {
                int aa = this.in.read();
                if (aa == -1) {
                    continueToRead = false;
                    break;
                }

                if (aa != 13 && aa != 10) {
                    eolRcvd = false;
                    sb.append((char)aa);
                } else if (aa == 13) {
                    thirteenRcvd = true;
                } else if (aa == 10) {
                    tenRcvd = true;
                }

                if (tenRcvd && thirteenRcvd && eolRcvd) {
                    continueToRead = false;
                    break;
                }

                if (tenRcvd && thirteenRcvd) {
                    eolRcvd = true;
                    thirteenRcvd = false;
                    tenRcvd = false;
                }
            }
        } catch (Exception var7) {
            throw new Exception("SophosConnection : readDoubleEOLReply() : Error reading reply : " + var7.toString());
        }

        return sb.toString();
    }

    private boolean checkResponse(String response, boolean firstResponse, boolean lastResponse) throws Exception {
        boolean validResponseOrVirusFree = false;

        try {
            if (firstResponse && !lastResponse) {
                if (response.indexOf("OK SSSP") == -1) {
                    throw new Exception("virusCheckUtilities : checkResponse : initial response from virus scan server indicated error/problem: " + response);
                }

                validResponseOrVirusFree = true;
            } else if (lastResponse && !firstResponse) {
                if (response.indexOf("BYE") == -1) {
                    throw new Exception("virusCheckUtilities : checkResponse : final response from virus scan server indicated error/problem: " + response);
                }

                validResponseOrVirusFree = true;
            } else {
                if (response.indexOf("REJ") != -1) {
                    throw new Exception("virusCheckUtilities : checkResponse : virus scan server rejected request: " + response);
                }

                if (response.indexOf("ACC") == -1) {
                    throw new Exception("virusCheckUtilities : checkResponse : virus scan server unexpected response: " + response);
                }

                if (response.indexOf("0000") != -1) {
                    validResponseOrVirusFree = true;
                } else if (response.indexOf("021E") != -1) {
                    validResponseOrVirusFree = true;
                } else if (response.indexOf("0203") != -1) {
                    validResponseOrVirusFree = false;
                } else {
                    if (response.indexOf("VIRUS") == -1) {
                        if (response.indexOf("ERROR") != -1) {
                            throw new Exception("virusCheckUtilities : checkResponse : virus scan server indicated error/problem: " + response);
                        }

                        throw new Exception("virusCheckUtilities : checkResponse : virus scan server indicated error/problem: " + response);
                    }

                    validResponseOrVirusFree = false;
                }
            }

            return validResponseOrVirusFree;
        } catch (Exception var6) {
            throw new Exception("SophosConnection : checkResponse() : Error reading response : " + var6.toString());
        }
    }

    public String getIpOrName() {
        return this.ipOrName;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
