package com.rueo.android.rueo.StringsParsing;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.rueo.android.rueo.MainActivity;

import java.util.Arrays;

public class StringParser {

    public static SpannableString addAHrefToEveryWord(SpannableString ss, final MainActivity con) {
        int wordLength = 0;
        Character[] tokens = {' ', '.', ',', '!', '?', '»', '«', ';', ':', '(', ')', '\u00a0', '\n'};
        final Character[] diacriticTokens = {'́'};
        Boolean wordEnded = false;
        for (int i = 0; i < ss.length(); i++) {
            if (!(Arrays.asList(tokens).contains(ss.charAt(i)))) {
                wordLength++;
                wordEnded = false;
            }
            if (i + 1 == ss.length())
                wordEnded = true;
            else if ((Arrays.asList(tokens).contains(ss.charAt(i + 1))))
                wordEnded = true;

            if (wordEnded) {
                Boolean isWord;
                if (wordLength > 0)
                    isWord = true;
                else
                    isWord = false;

                for (int j = i - (wordLength - 1); j <= i; j++) {
                    if (!Character.isLetter(ss.charAt(j)) && !Arrays.asList(diacriticTokens).contains(ss.charAt(j))) {
                        isWord = false;
                    }
                }

                if (isWord) {
                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            TextView tv = (TextView) textView;
                            Spanned s = (Spanned) tv.getText();
                            int start = s.getSpanStart(this);
                            int end = s.getSpanEnd(this);

                            String toCurWord = s.subSequence(start, end).toString();
                            toCurWord = toCurWord.replace(diacriticTokens[0].toString(), "");
                            con.searchBarEditDetectorEnabled = false;
                            con.curWord.setText(toCurWord);
                            con.curWord.setSelection(toCurWord.length());
                            con.searchBarEditDetectorEnabled = true;
                            con.startHttpRetrieveTask(toCurWord);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setUnderlineText(false);
                        }
                    }, i - (wordLength - 1), i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                wordLength = 0;
            }
        }
        return ss;
    }


}
