package org.fusesource.hawtjni.ui;

import static org.fusesource.hawtjni.ui.IOSupport.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.mozilla.interfaces.nsIChannel;
import org.mozilla.interfaces.nsIComponentManager;
import org.mozilla.interfaces.nsIComponentRegistrar;
import org.mozilla.interfaces.nsIFactory;
import org.mozilla.interfaces.nsIIOService;
import org.mozilla.interfaces.nsIInputStream;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsILoadGroup;
import org.mozilla.interfaces.nsIProtocolHandler;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIServiceManager;
import org.mozilla.interfaces.nsIStreamListener;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.xpcom.Mozilla;

public class MozillaSupport {

    String kSIMPLEURI_CONTRACTID = "@mozilla.org/network/simple-uri;1";
    String kIOSERVICE_CONTRACTID = "@mozilla.org/network/io-service;1";

    static interface UriHandler {
        InputStream open(URI uri) throws IOException;
    }

    private static final class ProtocolHandler implements nsIProtocolHandler {

        private final String protocol;
        private final UriHandler handler;

        private ProtocolHandler(String protocol, UriHandler handler) {
            this.protocol = protocol;
            this.handler = handler;
        }

        @Override
        public nsISupports queryInterface(String uuid) {
            if (uuid.equals(nsIProtocolHandler.NS_IPROTOCOLHANDLER_IID) || uuid.equals(nsIProtocolHandler.NS_ISUPPORTS_IID))
                return this;
            return null;
        }

        @Override
        public String getScheme() {
            return protocol;
        }

        @Override
        public int getDefaultPort() {
            return -1;
        }

        @Override
        public long getProtocolFlags() {
            return nsIProtocolHandler.URI_NOAUTH;
        }

        @Override
        public boolean allowPort(int port, String scheme) {
            return false;
        }

        @Override
        public nsIURI newURI(String spec, String originCharset, nsIURI baseURI) {
            nsIURI uri = create_nsIURI();
            uri.setSpec(spec);
            return uri;
        }

        @Override
        public nsIChannel newChannel(final nsIURI uri) {
            File file = null;
            try {
                InputStream is = handler.open(new URI(uri.getSpec()));
                file = File.createTempFile("temp", "tmp");
                transfer(is, file);
                System.out.println("creted tmp file: "+file);
            } catch (Throwable e) {
                file.delete();
                file = null;
            }

            nsIURI fileUri = create_nsIURI();
            if( file!=null ) {

                nsIChannel actualChannel = get_nsIIOService().newChannel(file.toURI().toASCIIString(), null, null);
                
                final File theFile = file;
                
                // Wrap the channel so we know when the transfer completes so we can 
                // delete the temp file.
                return new ChannelFilter(actualChannel) {
                    @Override
                    public void asyncOpen(final nsIStreamListener listener, final nsISupports context) {
                        next.asyncOpen(new StreamListenerFilter(listener) {
                            @Override
                            public void onStopRequest(nsIRequest aRequest, nsISupports aContext, long aStatusCode) {
                                try {
                                    next.onStopRequest(aRequest, aContext, aStatusCode);
                                } finally {
                                    if( theFile!=null ) {
                                        System.out.println("delted tmp file: "+theFile);
                                        theFile.delete();
                                    }
                                }
                            }                            
                        }, context);
                    }
                };
    
            } else {
                fileUri.setSpec("about:error");
                nsIChannel actualChannel = get_nsIIOService().newChannelFromURI(fileUri);
                return actualChannel;
            }
        }
    }
    
    static private class StreamListenerFilter implements nsIStreamListener {
        protected final nsIStreamListener next;
        private StreamListenerFilter(nsIStreamListener next) {
            this.next = next;
        }

        @Override
        public nsISupports queryInterface(String uuid) {
            if (uuid.equals(nsIStreamListener.NS_ISTREAMLISTENER_IID) 
                    || uuid.equals(nsIStreamListener.NS_IREQUESTOBSERVER_IID)
                    || uuid.equals(nsIStreamListener.NS_ISUPPORTS_IID))
                return this;
            return next.queryInterface(uuid);
        }

        @Override
        public void onDataAvailable(nsIRequest aRequest, nsISupports aContext, nsIInputStream aInputStream, long aOffset, long aCount) {
            next.onDataAvailable(aRequest, aContext, aInputStream, aOffset, aCount);
        }

        @Override
        public void onStartRequest(nsIRequest aRequest, nsISupports aContext) {
            next.onStartRequest(aRequest, aContext);
        }

        @Override
        public void onStopRequest(nsIRequest aRequest, nsISupports aContext, long aStatusCode) {
            next.onStopRequest(aRequest, aContext, aStatusCode);
        }
    }

    static class ChannelFilter implements nsIChannel {

        protected final nsIChannel next;

        public ChannelFilter(nsIChannel next) {
            this.next = next;
        }

        @Override
        public nsISupports queryInterface(String uuid) {
            if (uuid.equals(nsIChannel.NS_ICHANNEL_IID) 
                    || uuid.equals(nsIChannel.NS_IREQUEST_IID)
                    || uuid.equals(nsIChannel.NS_ISUPPORTS_IID))
                return this;
            return next.queryInterface(uuid);
        }

        @Override
        public void asyncOpen(nsIStreamListener aListener, nsISupports aContext) {
            next.asyncOpen(aListener, aContext);
        }

        @Override
        public String getContentCharset() {
            return next.getContentCharset();
        }

        @Override
        public int getContentLength() {
            return next.getContentLength();
        }

        @Override
        public String getContentType() {
            return next.getContentType();
        }

        @Override
        public nsIInterfaceRequestor getNotificationCallbacks() {
            return next.getNotificationCallbacks();
        }

        @Override
        public nsIURI getOriginalURI() {
            return next.getOriginalURI();
        }

        @Override
        public nsISupports getOwner() {
            return next.getOwner();
        }

        @Override
        public nsISupports getSecurityInfo() {
            return next.getSecurityInfo();
        }

        @Override
        public nsIURI getURI() {
            return next.getURI();
        }

        @Override
        public nsIInputStream open() {
            return next.open();
        }

        @Override
        public void setContentCharset(String aContentCharset) {
            next.setContentCharset(aContentCharset);
        }

        @Override
        public void setContentLength(int aContentLength) {
            next.setContentLength(aContentLength);
        }

        public void cancel(long aStatus) {
            next.cancel(aStatus);
        }

        public long getLoadFlags() {
            return next.getLoadFlags();
        }

        public nsILoadGroup getLoadGroup() {
            return next.getLoadGroup();
        }

        public String getName() {
            return next.getName();
        }

        public long getStatus() {
            return next.getStatus();
        }

        public boolean isPending() {
            return next.isPending();
        }

        public void resume() {
            next.resume();
        }

        public void setContentType(String aContentType) {
            next.setContentType(aContentType);
        }

        public void setLoadFlags(long aLoadFlags) {
            next.setLoadFlags(aLoadFlags);
        }

        public void setLoadGroup(nsILoadGroup aLoadGroup) {
            next.setLoadGroup(aLoadGroup);
        }

        public void setNotificationCallbacks(nsIInterfaceRequestor aNotificationCallbacks) {
            next.setNotificationCallbacks(aNotificationCallbacks);
        }

        public void setOriginalURI(nsIURI aOriginalURI) {
            next.setOriginalURI(aOriginalURI);
        }

        public void setOwner(nsISupports aOwner) {
            next.setOwner(aOwner);
        }

        public void suspend() {
            next.suspend();
        }
        
    }

    static public void installProtocol(final String protocol, String cid, final UriHandler handler) {
        String contractId = "@mozilla.org/network/protocol;1?name=" + protocol;
        nsIComponentRegistrar registrar = Mozilla.getInstance().getComponentRegistrar();
        registrar.registerFactory(cid, protocol, contractId, new nsIFactory() {
            public nsISupports queryInterface(String uuid) {
                if (uuid.equals(NS_IFACTORY_IID) || uuid.equals(NS_ISUPPORTS_IID))
                    return this;
                return null;
            }

            public nsISupports createInstance(nsISupports outer, String iid) {
                return new ProtocolHandler(protocol, handler);
            }

            public void lockFactory(boolean lock) {
            }
        });
    }

    static private nsIIOService get_nsIIOService() {
        return (nsIIOService) get_service("@mozilla.org/network/io-service;1", nsIIOService.NS_IIOSERVICE_IID);
    }
    
    static private nsIURI create_nsIURI() {
        return (nsIURI) create_component("@mozilla.org/network/standard-url;1", nsIURI.NS_IURI_IID);
    }

    static private nsISupports create_component(String cid, String iid) {
        nsIComponentManager componentManager = Mozilla.getInstance().getComponentManager();
        return componentManager.createInstanceByContractID(cid, null, iid);
    }

    static private nsISupports get_service(String cid, String iid) {
        nsIServiceManager serviceManager = Mozilla.getInstance().getServiceManager();
        return serviceManager.getServiceByContractID(cid, iid);
    }

}
