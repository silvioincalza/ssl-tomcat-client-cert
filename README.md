This is an all-in-one command that generates a certificate for the server and places it in a keystore file, while setting both the certifcate password and the keystore password.
The net result is a file called "server.jks". 

```
keytool -genkeypair -alias serverkey -keyalg RSA -dname "CN=Server,OU=Incalza team,O=Incalza,L=Como,S=CO,C=IT" -keypass s3cr3t -keystore server.jks -storepass s3cr3t
```

This is the all-in-one command that generates the certificate for the client and places it in a keystore file, while setting both the certificate password and the keystore password.
The net result is a file called "client.jks"

```
keytool -genkeypair -alias clientkey -keyalg RSA -dname "CN=Client,OU=Incalza team,O=Incalza,L=Como,S=CO,C=IT" -keypass s3cr3t -keystore client.jks -storepass s3cr3t
```

This command exports the client certificate.  
The net result is a file called "client-public.cer" in your home directory.

```
keytool -exportcert -alias clientkey -file client-public.cer -keystore client.jks -storepass s3cr3t 
keytool -exportcert -alias serverkey -file server-public.cer -keystore server.jks -storepass s3cr3t
```

This command imports the client certificate into the "server.jks" file.

```
keytool -importcert -keystore server.jks -alias clientcert -file client-public.cer -storepass s3cr3t -noprompt
```

This command create the p12 certificate for the browser.
```
keytool -importkeystore -srckeystore client.jks -destkeystore client.p12 -deststoretype PKCS12 -srcalias clientkey -deststorepass s3cr3t -destkeypass s3cr3t
```

Configure the connector on server.xml
```
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" SSLEnabled="true"
 maxThreads="150" scheme="https" secure="true"
 keystoreFile="${catalina.home}/conf/certs/server.jks" keystorePass="s3cr3t"
 truststoreFile="${catalina.home}/conf/certs/server.jks" truststorePass="s3cr3t"
 clientAuth="true" sslProtocol="TLS" />
```

Disable on server.xml	    
```       
<Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
```