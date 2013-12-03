package com.hipipal.texteditor.undo;

import com.hipipal.texteditor.common.Constants;

import android.text.Editable;

public interface TextChange extends Constants {

	/**
	 * Undo this change
	 * 
	 * @param text
	 *            the editable object on which the undo is done
	 * @return the caret position after the undo
	 */
	public int undo(Editable text);

	/**
	 * Method is deprecated, you should use the canMergeXXX methods instead
	 * 
	 * @return the caret position after this change
	 */
	@Deprecated
	public int getCaret();

	/**
	 * Method is deprecated, you should use the canMergeXXX methods instead
	 * 
	 * @param sequence
	 *            the sequence being appended to this {@link TextChange}
	 */
	@Deprecated
	public void append(CharSequence sequence);

	/**
	 * A change to the text {@linkplain s} will be made, where the
	 * {@linkplain count} characters starting at {@linkplain start} will be
	 * replaced by {@linkplain after} characters
	 * 
	 * If possible, this change is merged in this {@link TextChange}
	 * 
	 * @param s
	 *            the sequence being changed
	 * @param start
	 *            the start index
	 * @param count
	 *            the number of characters that will change
	 * @param after
	 *            the number of characters that will replace the old ones
	 * @return if the change can be merged with this {@link TextChange}
	 */
	public boolean canMergeChangeBefore(CharSequence s, int start, int count,
			int after);

	/**
	 * A change to the text {@linkplain s} has been made, where the
	 * {@linkplain count} characters starting at {@linkplain start} have
	 * replaced the substring of length {@linkplain before}
	 * 
	 * If possible, this change is merged in this {@link TextChange}
	 * 
	 * @param s
	 *            the sequence being changed
	 * @param start
	 *            the start index
	 * @param before
	 *            the number of character that were replaced
	 * @param count
	 *            the number of characters that will change
	 * @return if the change can be merged with this {@link TextChange}
	 */
	public boolean canMergeChangeAfter(CharSequence s, int start, int before,
			int count);

}
