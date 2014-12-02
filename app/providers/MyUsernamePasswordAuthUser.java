package providers;

import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
	private final boolean isTestDesigner;
	private final boolean isTestDeveloper;
	private final boolean isObserver;

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
		this.isTestDesigner = signup.isTestDesigner;
		this.isTestDeveloper = signup.isTestDeveloper;
		this.isObserver = signup.isObserver;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		name = null;
		isTestDesigner = false;
		isTestDeveloper = false;
		isObserver = false;
	}

	@Override
	public String getName() {
		return name;
	}

	public boolean isTestDesigner() {
		return isTestDesigner;
	}

	public boolean isTestDeveloper() {
		return isTestDeveloper;
	}

	public boolean isObserver() {
		return isObserver;
	}
	
	
}
