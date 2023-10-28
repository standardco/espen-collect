package org.espen.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.espen.collect.android.storage.StoragePathProvider;
import org.espen.collect.android.storage.StorageSubdirectory;
import org.espen.collect.android.support.rules.BlankFormTestRule;
import org.espen.collect.android.support.rules.TestRuleChain;

public class ExternalDataFileNotFoundTest {
    private static final String EXTERNAL_DATA_QUESTIONS = "external_data_questions.xml";

    public BlankFormTestRule activityTestRule = new BlankFormTestRule(EXTERNAL_DATA_QUESTIONS, "externalDataQuestions");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(activityTestRule);

    @Test
    public void questionsThatUseExternalFiles_ShouldDisplayFriendlyMessageWhenFilesAreMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        activityTestRule.startInFormEntry()
                .assertText(org.odk.collect.strings.R.string.file_missing, formsDirPath + "/external_data_questions-media/fruits.csv")
                .swipeToNextQuestion("External csv")
                .assertText(org.odk.collect.strings.R.string.file_missing, formsDirPath + "/external_data_questions-media/itemsets.csv");
    }
}
