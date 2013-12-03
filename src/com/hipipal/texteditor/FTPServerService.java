package com.hipipal.texteditor;

public class FTPServerService extends org.swiftp.FTPServerService {

	@Override
	protected Class<?> getSettingClass() {
		// TODO Auto-generated method stub
		return MFTPSettingAct.class;
	}
}