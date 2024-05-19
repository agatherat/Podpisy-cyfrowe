package org.example.signing;
import java.io.*;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;

//https://docs.oracle.com/javase%2Ftutorial%2F/security/apisign/gensig.html
public class SignatureGenerator {
    private String path;
    private String algorithm;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyPair keyPair;

    private Signature cipher;
    private String publicKeyFileName;
    private String signatureFileName;
    private String privateKeyFileName;
    public SignatureGenerator(String path, String algorithm, String signatureFileName,String privateKeyFileName, String publicKeyFileName){
        this.path=path;
        this.algorithm = algorithm;
        this.signatureFileName = signatureFileName;
        this.privateKeyFileName = privateKeyFileName;
        this.publicKeyFileName = publicKeyFileName;
    }

    public PrivateKey getPrivateKey(){
        return privateKey;
    }
    public PublicKey getPublicKey(){
        return publicKey;
    }

    public KeyPair getKeyPair(){
        return keyPair;
    }

    public Signature getSignature(){
        return cipher;
    }

    public String getHashFunction(String algorithm){
        //algorytmy podpisu cyfrowego łączą funkcję skrótu z algorytmem podpisu.
        // Różne algorytmy podpisu wymagają różnych kombinacji funkcji skrótu i algorytmu podpisu
        return switch (algorithm) {
            case "DSA" -> "SHA1withDSA";
            case "RSA" -> "SHA256withRSA";
            default -> "error";
        };
    }


    public void generateSignature(){
        try{
            //Generate a key pair (public key and private key). The private key is needed for signing the data.
            //The public key will be used by the VerSig program for verifying the signature.

            //A key pair is generated by using the KeyPairGenerator class.
            // generate a public/private key pair for the DSA. Keys will be with a 1024-bit length.
            KeyPairGenerator keyGenerated = KeyPairGenerator.getInstance(algorithm);

            //SecureRandom class provides a cryptographically strong random number generator
            //generowania kryptograficznie silnych liczb losowych
//            if(algorithm.equals("EC")){
//                SecureRandom random = SecureRandom.getInstanceStrong(); // Użyj silnego generatora liczb losowych
//                keyGenerated.initialize(new ECGenParameterSpec("secp256r1"), random);
//            }
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGenerated.initialize(1024, random);

            //Generate the key pair and store the keys in PrivateKey and PublicKey objects.
            keyPair = keyGenerated.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            //A digital signature is created (or verified) using an instance of the Signature class
            String hashFunction = getHashFunction(algorithm);
            //Get a Signature object for generating or verifying signatures using the DSA algorithm
            cipher = Signature.getInstance(hashFunction);
            cipher.initSign(privateKey);

            //read in the data a buffer at a time and supply it to the Signature object by update method
            FileInputStream file = new FileInputStream(path);
            BufferedInputStream inputStrem = new BufferedInputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStrem.read(buffer)) >= 0) {
                cipher.update(buffer, 0, length);
            };
            inputStrem.close();

            //generate the digital signature of that data
            byte [] signature = cipher.sign();

            //save the signature in a file
            saveDataToFiles(signature, publicKey, privateKey);

        } catch (Exception e) {
            System.err.println("Exception occured " + e.toString());
        }
    }

    private void saveDataToFiles(byte[] signature, PublicKey publicKey, PrivateKey privateKey) throws IOException {
        FileOutputStream fileSignature = new FileOutputStream(signatureFileName);
        fileSignature.write(signature);
        fileSignature.close();

        // save the public key in a file
        byte[] key = publicKey.getEncoded();
        FileOutputStream fileKey = new FileOutputStream(publicKeyFileName);
        fileKey.write(key);
        fileKey.close();

        if(privateKeyFileName!=null) {
            byte[] priv = privateKey.getEncoded();
            FileOutputStream privateFileKey = new FileOutputStream(privateKeyFileName);
            privateFileKey.write(priv);
            privateFileKey.close();
        }
    }

}