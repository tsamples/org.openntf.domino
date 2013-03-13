package org.openntf.domino;

public interface Newsletter extends Base<lotus.domino.Newsletter>, lotus.domino.Newsletter {

	@Override
	public lotus.domino.Document formatDocument(lotus.domino.Database database, int index);

	@Override
	public lotus.domino.Document formatMsgWithDoclinks(lotus.domino.Database database);

	@Override
	public Session getParent();

	@Override
	public String getSubjectItemName();

	@Override
	public boolean isDoScore();

	@Override
	public boolean isDoSubject();

	@Override
	public void setDoScore(boolean flag);

	@Override
	public void setDoSubject(boolean flag);

	@Override
	public void setSubjectItemName(String name);

}
