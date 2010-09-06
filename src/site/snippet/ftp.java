START SNIPPET: simple
FTPClient client = new FTPClient();

client.connect( "server" );
client.login( "user", "pass" );

FTPFile files[] = client.listFiles();

for (FTPFile file : files) 
    System.out.println(file.getName());
END SNIPPET: simple

START SNIPPET: filedownload0
boolean success = client.download("remote", "local");
END SNIPPET: filedownload0

START SNIPPET: filedownload1
// Open a stream for local file storage
OutputStream fos = new FileOutputStream("/tmp/readme.txt");
// Get the file from the remote server
client.retrieveFile("readme.txt", fos);
// close the output stream
fos.close();
END SNIPPET: filedownload1

START SNIPPET: filedownload2
BufferedInputStream bis = new BufferedInputStream(client.retrieveFileStream("readme.txt"));
byte[] buf = new byte[8192];
int read = 0;
while ((read = bis.read(buf)) != -1)
	System.out.println(new String(buf,0,read));
bis.close();

// This should be done before executing subsequent commands
client.completePendingCommand();
END SNIPPET: filedownload2
