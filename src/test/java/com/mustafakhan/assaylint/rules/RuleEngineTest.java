package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleEngineTest {

    private static ValidationRule fixedRule(String name, int line) {
        return new ValidationRule() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
                return List.of(new ValidationIssue(line, "S" + line, name, Severity.ERROR, "problem"));
            }
        };
    }

    @Test
    void combinesIssuesFromAllRulesSortedByLine() {
        RuleEngine engine = new RuleEngine(List.of(fixedRule("B", 7), fixedRule("A", 3)));

        List<ValidationIssue> issues = engine.run(List.of(), CONFIG);

        assertEquals(List.of(3, 7), issues.stream().map(ValidationIssue::line).toList());
    }
}
