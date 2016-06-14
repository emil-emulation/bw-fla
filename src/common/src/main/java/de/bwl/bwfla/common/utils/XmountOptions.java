package de.bwl.bwfla.common.utils;

import de.bwl.bwfla.common.utils.EmulatorUtils.XmountOutputFormat;

public class XmountOptions {
	private boolean readonly = false;
	private XmountOutputFormat outFmt;
	private long offset = 0;
	private long size = -1;
	
	public XmountOptions() {
		outFmt = XmountOutputFormat.RAW;
	}
	public XmountOptions(XmountOutputFormat outFmt)
	{
		this.outFmt = outFmt;
	}
	
	public void setOffset(long off) {
		this.offset = off;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public boolean isReadonly()
	{
		return readonly;
	}
	
	public XmountOutputFormat getOutFmt()
	{
		return outFmt;
	}
	
	public void setXmountOptions(ProcessRunner process)
	{
		process.addArguments("--out", outFmt.toString());
		if (!readonly) {
			process.addArguments("--cache", "writethrough");
			process.addArguments("--inopts", "qemuwritable=true,bdrv_cache=writeback");
		}
		
		if(size >= 0)
		{
			process.addArguments("--morph", "trim");
			String morphOpts;
			if(offset == 0)
				morphOpts = "size=" + size;
			else
				morphOpts = "offset=" + offset +",size=" + size;
			process.addArguments("--morphopts", morphOpts);
		}
	}
}
