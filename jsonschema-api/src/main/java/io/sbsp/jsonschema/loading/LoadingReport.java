package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.loading.LoadingIssue.LoadingIssueBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class LoadingReport {

    @Getter
    private final List<LoadingIssue> issues = new ArrayList<>();
    private boolean hasError = false;

    public LoadingReport warn(LoadingIssueBuilder issue) {
        checkNotNull(issue, "issue must not be null");
        issues.add(issue.level(LoadingIssueLevel.WARN).build());
        return this;
    }

    public LoadingReport error(LoadingIssueBuilder issue) {
        checkNotNull(issue, "issue must not be null");
        issues.add(issue.level(LoadingIssueLevel.ERROR).build());
        hasError = true;
        return this;
    }

    public LoadingReport log(LoadingIssue issue) {
        checkNotNull(issues, "issues must not be null");
        issues.add(issue);
        if (issue.getLevel() == LoadingIssueLevel.ERROR) {
            hasError = true;
        }
        return this;
    }

    public boolean hasErrors() {
        return hasError;
    }

    @Override
    public String toString() {
        if (!hasErrors()) {
            return "No errors";
        }
        StringBuilder output = new StringBuilder("");
        output.append(issues.size() + " errors found while loading:\n");
        for (LoadingIssue issue : issues) {
            output.append("\t" + issue.toString() + "\n");
        }
        return output.toString();
    }
}
