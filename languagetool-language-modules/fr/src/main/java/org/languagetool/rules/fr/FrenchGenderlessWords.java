/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.fr;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;

import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.synthesis.Synthesizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;

/**
 * A rule that matches spaces before ?,:,; and ! (required for correct French
 * punctuation).
 *
 * @see <a href=
 *      "http://unicode.org/udhr/n/notes_fra.html">http://unicode.org/udhr/n/notes_fra.html</a>
 *
 * @author Marcin Miłkowski
 */
public class FrenchGenderlessWords extends Rule {

  private Synthesizer frenchSynth;

  public FrenchGenderlessWords(Language language){
    frenchSynth = language.createDefaultSynthesizer();
  }

  

  @Override
  public String getId() {
    return "FRENCH_GENDERLESS";
  }

  @Override
  public String getDescription() {
    return "2criture Inclusive";
  }


  // This is the method with the error detection logic that you need to implement:
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();

    // Let's get all the tokens (i.e. words) of this sentence, but not the spaces:
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    
    // No let's iterate over those - note that the first token will
    // be a special token that indicates the start of a sentence:
    for (AnalyzedTokenReadings token : tokens) {
      
      System.out.println("Token: " + token.getToken());  // the original word from the input text
      
      // A word can have more than one reading, e.g. 'dance' can be a verb or a noun,
      // so we iterate over the readings:
      for (AnalyzedToken analyzedToken : token.getReadings()) {
        try {
          System.out.println("  Féminin : " + Arrays.deepToString(frenchSynth.synthesize(analyzedToken,"N f p")));
        }
        catch(IOException e) {
          System.out.println("  Féminin : None");
        }
        try {
          System.out.println("  Masculin : " + Arrays.deepToString(frenchSynth.synthesize(analyzedToken,"N m p")));
        }
        catch(IOException e) {
          System.out.println("  Masculin : None");
        }


        System.out.println("  Lemma: " + analyzedToken.getLemma());
        System.out.println("  POS: " + analyzedToken.getPOSTag());
      }
      
      // You can add your own logic here to find errors. Here, we just consider
      // the word "demo" an error and create a rule match that LanguageTool will
      // then show to the user:
      if (token.getToken().equals("téléphone")) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), "The demo rule thinks this looks wrong");
        ruleMatch.setSuggestedReplacement("blablah");  // the user will see this as a suggested correction
        ruleMatches.add(ruleMatch);
      }
    }

    return toRuleMatchArray(ruleMatches);
  }

}
