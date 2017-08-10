# Certificates #

## CA and trust keystore

	keytool -genkeypair -keyalg RSA -keysize 2048 -validity 365 -alias ca -dname "CN=ca,O=incalza,S=IT" -keystore ca.jks -storepass password
	keytool -exportcert -rfc -alias ca -keystore ca.jks -storepass password > ca.pem
	cat ca.pem | keytool -importcert -alias ca -noprompt -keystore trust.jks -storepass password

## server cert

Notice that the CN must be equal to the DNS hostname!

	keytool -genkeypair -keyalg RSA -keysize 2048 -validity 365 -alias server -dname "CN=localhost.com,O=incalza,S=CH" -keystore server.jks -storepass password
	keytool -certreq -alias server -storepass password -keystore server.jks | keytool  -gencert -alias ca -rfc -keystore ca.jks -storepass password > server.pem
	cat ca.pem | keytool -importcert -alias ca -noprompt -keystore server.jks -storepass password
	cat ca.pem server.pem | keytool -importcert -alias server -keystore server.jks -storepass password

## client cert

The username of the client is the value of CN!

	keytool -genkeypair -keyalg RSA -keysize 2048 -validity 365 -alias client -dname "CN=silvio@incalza.me,O=incalza,S=CH" -keystore client.jks -storepass password
	keytool -certreq -alias client -keystore client.jks -storepass password | keytool -gencert -alias ca -rfc -keystore ca.jks -storepass password> client.pem
	cat ca.pem | keytool -importcert -alias ca -noprompt -keystore client.jks -storepass password
	cat ca.pem client.pem | keytool -importcert -alias client -keystore client.jks -storepass password


# Server authentication and authorization #

Tomcat and Jetty authenticates the client if the certificate if signed by a trusted CA. 
However, standard Java Web security is a mess to configure and I decided to use Spring Security to provide authorization.

## tomcat

In server.xml:

	<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" SSLEnabled="true"
	           maxThreads="150" scheme="https" secure="true"
	           keystoreFile="server.jks" keystorePass="password"
	           truststoreFile="trust.jks" truststorePass="password"
	           clientAuth="want" sslProtocol="TLS" />

## Jetty

	SslContextFactory sslContextFactory = new SslContextFactory();
	sslContextFactory.setKeyStoreInputStream(new FileInputStream("server.jks"));
	sslContextFactory.setKeyStorePassword("password");
	sslContextFactory.setKeyManagerPassword("password");
	sslContextFactory.setTrustStoreInputStream(new FileInputStream("trust.jks"));
	sslContextFactory.setTrustStorePassword("password");
	sslContextFactory.setWantClientAuth(true);
	_connector = new SslSelectChannelConnector(sslContextFactory);

## Spring Security - authorization

	<x509 subject-principal-regex="CN=(.*)"/>

optionally user-service-ref="userDetailsService" 

# Client code #

## HttpClient

	final KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
	truststore.load(new FileInputStream("trust.jks"), "password".toCharArray());

	final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	keystore.load(new FileInputStream("client.jks"), "password".toCharArray());

	Scheme httpsScheme = new Scheme("https", 443, new SSLSocketFactory(keystore, "password", truststore));

	SchemeRegistry schemeRegistry = new SchemeRegistry();
	schemeRegistry.register(httpsScheme);

	ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
	return new DefaultHttpClient(cm);

# Jetty WebSocketClient

	factory.getSslContextFactory().setTrustAll(false);
	factory.getSslContextFactory().setTrustStore(truststore);
	factory.getSslContextFactory().setKeyStore(keystore);
	factory.getSslContextFactory().setKeyManagerPassword("password");



# For the following commands, set the values in parenthesis to be whatever makes sense for your environment.  The parenthesis are not necessary for the command.

# This is an all-in-one command that generates a certificate for the server and places it in a keystore file, while setting both the certifcate password and the keystore password.
# The net result is a file called "server.jks". 

keytool -genkeypair -alias serverkey -keyalg RSA -dname "CN=Server,OU=Incalza team,O=Incalza,L=Como,S=CO,C=IT" -keypass s3cr3t -keystore server.jks -storepass s3cr3t

# This is the all-in-one command that generates the certificate for the client and places it in a keystore file, while setting both the certificate password and the keystore password.
# The net result is a file called "client.jks"

keytool -genkeypair -alias clientkey -keyalg RSA -dname "CN=Client,OU=Incalza team,O=Incalza,L=Como,S=CO,C=IT" -keypass s3cr3t -keystore client.jks -storepass s3cr3t

# This command exports the client certificate.  
# The net result is a file called "client-public.cer" in your home directory.

keytool -exportcert -alias clientkey -file client-public.cer -keystore client.jks -storepass s3cr3t 
keytool -exportcert -alias serverkey -file server-public.cer -keystore server.jks -storepass s3cr3t

# This command imports the client certificate into the "server.jks" file.

keytool -importcert -keystore server.jks -alias clientcert -file client-public.cer -storepass s3cr3t -noprompt