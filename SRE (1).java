import java.util.ArrayList;
import java.util.List;


public class SRE {

    /**
     * Pattern group contains all possible values for this group and indication,
     * whether the group can be repeated in the resulting string
     */
    static class Group {
        List<GroupEntry> possibleValues;
        boolean singleOccurrence;

        public Group() {
            possibleValues = new ArrayList<>();
            singleOccurrence = true;
        }
    }

    /**
     * One single possible value of the pattern group with an indication,
     * whether the value can be repeated in the resulting string
     */
    static class GroupEntry {
        String value;
        boolean singleOccurrence = true;
    }

    /**
     * Parsed regex pattern
     */
    private List<Group> groups;

    /**
     * Factory method for this class. Checks correctness of the
     * pattern (not-empty, matching parenthesis and containing
     * only legal characters). And then parses the regex into
     * groups list
     * @param sre regex pattern
     * @return newly created instance of SRE class
     */
	public static SRE parse(String sre) {
		// check correctness
        if (sre == null || sre.isEmpty())
            throw new IllegalArgumentException("Empty regex");
        int parentheses = 0;
        for (int i = 0; i < sre.length(); i++) {
            int ch = sre.charAt(i);
            if (ch == '(')
                parentheses++;
            else if (ch == ')')
                parentheses--;
            if (!((ch >= 'a' && ch <= 'z') || ch == '|' || ch == '*' || ch == '(' || ch == ')'))
                throw new IllegalArgumentException("Illegal characters on the regex (" + ch + ")");
        }
        if (parentheses != 0)
            throw new IllegalArgumentException("Parentheses are not matching");
        // create groups
        int len = sre.length();
        SRE result = new SRE();
        result.groups = new ArrayList<>();
        Group current = new Group();
        result.groups.add(current);
        int groupStarts = 0, groupEnds = 0;
        while (groupEnds < len) {
            while (groupStarts < len && sre.charAt(groupStarts) == '(')
                groupStarts++;
            groupEnds = groupStarts;
            while (groupEnds < len && !(sre.charAt(groupEnds) == '*' || sre.charAt(groupEnds) == '('
                    || sre.charAt(groupEnds) == '|' || sre.charAt(groupEnds) == ')'))
                groupEnds++;
            if (groupEnds > groupStarts) {
                GroupEntry entry = new GroupEntry();
                entry.value = sre.substring(groupStarts, groupEnds);
                entry.singleOccurrence = true;
                current.possibleValues.add(entry);
            } else
                break;
            if (groupEnds < len) {
                if (sre.charAt(groupEnds) == '*') {
                    current.possibleValues.get(current.possibleValues.size() - 1).singleOccurrence = false;
                    groupEnds++;
                }
                if (groupEnds+1 < len && sre.charAt(groupEnds) == ')' && sre.charAt(groupEnds+1) == '*')
                    current.singleOccurrence = false;
                if (sre.charAt(groupEnds) != '|') {
                    current = new Group();
                    result.groups.add(current);
                }
                groupStarts = groupEnds+1;
            }
        }
        // Remove empty groups (without possible values)
        List<Group> toRemove = new ArrayList<>();
        for (Group g : result.groups)
            if (g.possibleValues.size() == 0)
                toRemove.add(g);
            // If there is only one possible value and it can be
            // repeated, the group can be repeated as well
            else if (g.possibleValues.size() == 1)
                g.singleOccurrence = g.possibleValues.get(0).singleOccurrence;
        result.groups.removeAll(toRemove);
        return result;
    }

	public boolean matches(String s) {
	    // Deep clone, so we can remove groups from the list
        // and still keep the pattern for processing next strings
	    List<Group> localGroups = new ArrayList<>();
	    for (Group g : groups) {
	        Group gNew = new Group();
	        gNew.singleOccurrence = g.singleOccurrence;
	        localGroups.add(gNew);
	        for (GroupEntry ge : g.possibleValues) {
	            GroupEntry geNew = new GroupEntry();
	            geNew.value = ge.value;
	            geNew.singleOccurrence = ge.singleOccurrence;
	            gNew.possibleValues.add(geNew);
            }
        }
        // While we either processed the whole string or run out of
        // available patterns, we compare beginning of the string with
        // one of the pattern values. If they match, we remove this
        // recognized beginning from the string
        // If pattern group is not repeatable, we remove it from the
        // groups list
	    while (localGroups.size() > 0 && s.length() > 0) {
	        // We always work only with first pattern group
            // If there is no matches and this group is repeatable
            // (not mandatory), we remove it from the list
	        Group current = localGroups.get(0);
	        boolean hadChanges = false;
	        for (GroupEntry ge : current.possibleValues) {
	            // If pattern value is not repeatable, we
                // just compare it to the beginning of the string
	            if (ge.singleOccurrence) {
	                if (s.startsWith(ge.value)) {
	                    s = s.substring(ge.value.length());
	                    hadChanges = true;
	                    break;
                    }
                // If pattern value is repeatable, we concatenate it
                // with itself until beginning of the string contains
                // repeating pattern value
                } else {
                    String pattern = "";
	                while (s.startsWith(pattern + ge.value))
	                    pattern += ge.value;
	                if (pattern.length() > 0) {
                        s = s.substring(pattern.length());
                        hadChanges = true;
                        break;
                    }
                }
            }
            // If group didn't allow repetition and we
            // found its occurrence in the beginning of
            // the string, we remove it from groups list
            if (current.singleOccurrence) {
                if (hadChanges)
                    localGroups.remove(0);
                else
                    // If group is mandatory and none of the possible
                    // values match beginning of the string, the whole
                    // string doesn't match the regex
                    return false;
            } else {
                // If group is not mandatory and none of the possible
                // values match beginning of the string, we move to the
                // next group
                if (!hadChanges)
                    localGroups.remove(0);
            }
        }
        // If we processed the whole string, it matches the regex
        // If we run out of regex groups, the string doesn't match
        return s.length() == 0;
	}

    /**
     * Class entry point, tests the algorithm
     * @param args ignored
     */
	public static void main(String[] args) {
	    String regex = "a(b*|c)d";
	    SRE parser = SRE.parse(regex);
	    String value = "abbbbbbd";
	    boolean match = parser.matches(value);
	    System.out.println("String \"" + value + (match ? "\"" : "\" not")
                + " matches pattern \"" + regex + "\" (" + String.valueOf(match) + ")");
    }
}
