package org.netling.ftp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.netling.ftp.FTPFile.Access;
import org.netling.ftp.FTPFile.Permission;


public class FTPFileTest {
	
	@Test
	public void testFilePermissions() {
		// User-leve perms only (700)
		FTPFile file = new FTPFile();
		file.setPermission(Access.USER, Permission.READ);
		file.setPermission(Access.USER, Permission.WRITE);
		file.setPermission(Access.USER, Permission.EXECUTE);
		assertEquals("rwx------", file.getPermissionsString());
		
		// No permissions
		file = new FTPFile();
		assertEquals("---------", file.getPermissionsString());
		
		// 755
		file = new FTPFile();
		file.setPermission(Access.USER, Permission.READ);
		file.setPermission(Access.USER, Permission.WRITE);
		file.setPermission(Access.USER, Permission.EXECUTE);
		file.setPermission(Access.GROUP, Permission.READ);
		file.setPermission(Access.GROUP, Permission.EXECUTE);
		file.setPermission(Access.WORLD, Permission.READ);
		file.setPermission(Access.WORLD, Permission.EXECUTE);
		assertEquals("rwxr-xr-x",file.getPermissionsString());
		
	}
	

}
