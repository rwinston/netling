/*
 * Copyright 2010 netling project <http://netling.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file may incorporate work covered by the following copyright and
 * permission notice:
 *
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing,
 *      software distributed under the License is distributed on an
 *      "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *      KIND, either express or implied.  See the License for the
 *      specific language governing permissions and limitations
 *      under the License.
 */
package org.netling.ftp;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;

/***
 * The FTPFile class is used to represent information about files stored
 * on an FTP server.
 * <p>
 * <p>
 * @see FTPFileEntryParser
 * @see FTPClient#listFiles
 ***/

public class FTPFile {

	/**
	 * An enumeration that represents the file type
	 */
    public enum Type {
    	FILE,
    	DIRECTORY,
    	SYMBOLIC_LINK,
    	UNKNOWN
    }
    
    /**
     * An enumeration that represents the access permission type
     */
    public enum Access {
    	USER,
    	GROUP,
    	WORLD;
    }
    
    /**
     * An enumeration that represents the permission level
     */
    public enum Permission {
    	READ, 
    	WRITE,
    	EXECUTE;
    	
    	public String toString() {
    		if (this==READ)
    			return "r";
    		else if (this==WRITE)
    			return "w";
    		else 
    			return "x";
    	};	
    }

    private Type type; 
    private int hardLinkCount;
    private long size;
    private String rawListing, user, group, name, link;
    private Calendar date;
    /** Permission map (typedefs would be nice here if we had them!) */
    private EnumMap<Access, EnumSet<Permission>> permissions;

    /*** Creates an empty FTPFile. ***/
    public FTPFile()
    {
        permissions = new EnumMap<FTPFile.Access, EnumSet<Permission>>(Access.class);
        
        permissions.put(Access.USER, EnumSet.noneOf(Permission.class));
        permissions.put(Access.GROUP, EnumSet.noneOf(Permission.class));
        permissions.put(Access.WORLD, EnumSet.noneOf(Permission.class));
        
        rawListing = null;
        type = Type.UNKNOWN;
        hardLinkCount = 0;
        size = 0;
        user = null;
        group = null;
        date = null;
        name = null;
    }


    /***
     * Set the original FTP server raw listing from which the FTPFile was
     * created.
     * 
     * @param rawListing  The raw FTP server listing.
     ***/
    public void setRawListing(String rawListing)
    {
        this.rawListing = rawListing;
    }

    /***
     * Get the original FTP server raw listing used to initialize the FTPFile.
     * 
     * @return The original FTP server raw listing used to initialize the
     *         FTPFile.
     ***/
    public String getRawListing()
    {
        return rawListing;
    }


    /***
     * Determine if the file is a directory.
     * 
     * @return True if the file is of type <code>DIRECTORY_TYPE</code>, false if
     *         not.
     ***/
    public boolean isDirectory()
    {
        return (type == Type.DIRECTORY);
    }

    /***
     * Determine if the file is a regular file.
     * 
     * @return True if the file is of type <code>FILE_TYPE</code>, false if
     *         not.
     ***/
    public boolean isFile()
    {
        return (type == Type.FILE);
    }

    /***
     * Determine if the file is a symbolic link.
     * 
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isSymbolicLink()
    {
        return (type == Type.SYMBOLIC_LINK);
    }

    /***
     * Determine if the type of the file is unknown.
     * 
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isUnknown()
    {
        return (type == Type.UNKNOWN);
    }


    /***
     * Set the type of the file 
     * 
     * @param type  The {@link Type} instance representing the type of the file.
     ***/
    public void setType(final Type type)
    {
        this.type = type;
    }


    /***
     * Return the type of the file 
     * e.g., if it is a directory, a regular file, or a symbolic link.
     * 
     * @return The type of the file.
     ***/
    public Type getType()
    {
        return type;
    }


    /***
     * Set the name of the file.
     * 
     * @param name  The name of the file.
     ***/
    public void setName(String name)
    {
        this.name = name;
    }

    /***
     * Return the name of the file.
     * 
     * @return The name of the file.
     ***/
    public String getName()
    {
        return name;
    }


    /**
     * Set the file size in bytes.
     * @param size The file size in bytes.
     */
    public void setSize(long size)
    {
        this.size = size;
    }


    /***
     * Return the file size in bytes.
     * 
     * @return The file size in bytes.
     ***/
    public long getSize()
    {
        return size;
    }


    /***
     * Set the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     * 
     * @param links  The number of hard links to this file.
     ***/
    public void setHardLinkCount(int links)
    {
        hardLinkCount = links;
    }


    /***
     * Return the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     * 
     * @return The number of hard links to this file.
     ***/
    public int getHardLinkCount()
    {
        return hardLinkCount;
    }


    /***
     * Set the name of the group owning the file.  This may be
     * a string representation of the group number.
     * 
     * @param group The name of the group owning the file.
     ***/
    public void setGroup(String group)
    {
        this.group = group;
    }


    /***
     * Returns the name of the group owning the file.  Sometimes this will be
     * a string representation of the group number.
     * 
     * @return The name of the group owning the file.
     ***/
    public String getGroup()
    {
        return group;
    }


    /***
     * Set the name of the user owning the file.  This may be
     * a string representation of the user number;
     * 
     * @param user The name of the user owning the file.
     ***/
    public void setUser(String user)
    {
        this.user = user;
    }

    /***
     * Returns the name of the user owning the file.  Sometimes this will be
     * a string representation of the user number.
     * 
     * @return The name of the user owning the file.
     ***/
    public String getUser()
    {
        return user;
    }


    /***
     * If the FTPFile is a symbolic link, use this method to set the name of the
     * file being pointed to by the symbolic link.
     * 
     * @param link  The file pointed to by the symbolic link.
     ***/
    public void setLink(String link)
    {
        this.link = link;
    }


    /***
     * If the FTPFile is a symbolic link, this method returns the name of the
     * file being pointed to by the symbolic link.  Otherwise it returns null.
     * 
     * @return The file pointed to by the symbolic link (null if the FTPFile
     *         is not a symbolic link).
     ***/
    public String getLink()
    {
        return link;
    }


    /***
     * Set the file timestamp.  This usually the last modification time.
     * The parameter is not cloned, so do not alter its value after calling
     * this method.
     * 
     * @param date A Calendar instance representing the file timestamp.
     ***/
    public void setTimestamp(Calendar date)
    {
        this.date = date;
    }


    /***
     * Returns the file timestamp.  This usually the last modification time.
     * 
     * @return A Calendar instance representing the file timestamp.
     ***/
    public Calendar getTimestamp()
    {
        return date;
    }


    /***
     * Set the given permission for the specified access group.
     * 
     * @param access The access group (An {@link Access} instance)
     * @param permission The access permission (A {@link Permission} instance)
     * TODO do we need a removePerm?
     * 
     ***/
    public void setPermission(Access access, Permission permission)
    {
        EnumSet<Permission> perms = permissions.get(access);
        perms.add(permission);
        permissions.put(access, perms);
    }


    /***
     * Determines if the given access group has the given access permission.
     * 
     * @param access The access group (An {@link Access} instance)
     * @param permission A {@link Permission} instance
     ***/
    public boolean hasPermission(Access access, Permission permission)
    {
        EnumSet<Permission> perms = permissions.get(access);
        if (perms == null)
        	return false;
        else
        	return perms.contains(permission); 
    }


    /***
     * Returns a string representation of the FTPFile information.  This
     * will be the raw FTP server listing that was used to initialize the
     * FTPFile instance.
     * 
     * @return A string representation of the FTPFile information.
     ***/
    @Override
    public String toString()
    {
        return rawListing;
    }
    
    /**
     * Return the permissions as a human-readable string
     * @ TODO test
     * @return
     */
    public String getPermissionsString() {
    	final StringBuilder buf= new StringBuilder();
    	// Rely on the natural ordering of EnumMap iterators
    	for (final Entry<Access, EnumSet<Permission>> access : permissions.entrySet()) {
    		final EnumSet<Permission> permSet = access.getValue();
    		for (Permission permission : Permission.values()) {
    			if (permSet.contains(permission))
    				buf.append(permission);
    			else
    				buf.append("-");
    		}
    	}
    	return buf.toString();
    }

}
