START SNIPPET: simple
import org.netling.ssh.SSHClient;
import org.netling.ssh.connection.channel.direct.Session;
import org.netling.ssh.connection.channel.direct.Session.Command;

SSHClient client = new SSHClient();

// Initialize from standard known hosts file
client.loadKnownHosts();

client.connect("myserver");

// By default will look in $HOME/.ssh for key files
client.authPublickey("joe");

Session session = client.startSession();
Command command = session.exec("ls");
System.out.println(command.getOutputAsString());
client.disconnect();
END SNIPPET: simple