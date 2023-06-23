/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
**********************************************************************/
package com.ibm.bear.qa.spot.wb.utils.security;

import java.io.*;

import org.apache.commons.codec.binary.Base64;

public class EncryptPasswords {

/**
 * Encrypt properties value assuming that arguments are made of property
 * name/value peer.
 * <p>
 * The result is written in password.properties file<br>
 * For example: testPassword Passw0rd!
 * </p><p>
 * Note that if only one argument is provided, it assumes that it's an encrypted
 * value and try to decrypt it.
 * </p>
 * @param args
 */
public static void main(final String[] args) {
	if (args.length == 1) {
		decrypt(args);
	} else {
		encrypt(args);
	}
}

private static void decrypt(final String[] args) {
	Base64 base64 = new Base64();
	System.out.println(new String(base64.decode(args[0].getBytes())));
}

private static void encrypt(final String[] args) {
	File file = new File("password.properties");
	PrintWriter writer = null;
	Base64 base64 = new Base64();
	StringBuilder builder = new StringBuilder();
	try {
		writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file, false)), false);
		for (int i=0; i<args.length; i+=2) {
			StringBuilder lineBuilder = new StringBuilder(args[i]);
			lineBuilder.append("Password=");
			byte[] data = args[i+1].getBytes();
			lineBuilder.append(new String(base64.encode(data)));
			lineBuilder.append(System.getProperty("line.separator"));
			writer.append(lineBuilder);
			builder.append(lineBuilder);
		}
	}
	catch (IOException ioe) {
		System.err.println(ioe.getMessage());
	}
	finally {
		if (writer != null) {
			writer.close();
		}
	}
	if (file.exists()) {
		System.out.println("Following passwords have been encrypted and written in properties file "+file.getAbsolutePath()+":");
		System.out.println(builder.toString());
	} else {
		System.err.println("File "+file.getAbsolutePath()+" does not exists!");
	}
}

}
