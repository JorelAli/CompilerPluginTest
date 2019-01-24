import javax.lang.model.element.Name;

public class ASTHelper {

	public static Name makeName(String name) {
		return new Name() {

			@Override
			public char charAt(int index) {
				return name.charAt(index);
			}

			@Override
			public int length() {
				return name.length();
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return name.substring(start, end);
			}

			@Override
			public boolean contentEquals(CharSequence cs) {
				return name.contentEquals(cs);
			}

		};
	}

	public static com.sun.tools.javac.util.Name makeSunName(String name) {
		return (com.sun.tools.javac.util.Name) makeName(name);
	}

}
