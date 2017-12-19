package box2dLight;

import com.badlogic.gdx.Gdx;

/**
 * Helper class that stores source and destination factors for blending
 * 
 * @author rinold
 */
public class BlendFunc {
	
	final int default_sfactor;
	final int default_dfactor;
	int sfactor;
	int dfactor;
	
	public BlendFunc(int sFactor, int dFactor) {
		this.default_sfactor = sFactor;
		this.default_dfactor = dFactor;
		this.sfactor = sFactor;
		this.dfactor = dFactor;
	}
	
	/**
	 * Sets source and destination blending factors
	 */
	public void set(int sFactor, int dFactor) {
		this.sfactor = sFactor;
		this.dfactor = dFactor;
	}
	
	/**
	 * Resets source and destination blending factors to default values
	 * that were set on instance creation
	 */
	public void reset() {
		sfactor = default_sfactor;
		dfactor = default_dfactor;
	}
	
	/**
	 * Calls glBlendFunc with own source and destination factors
	 */
	public void apply() {
		Gdx.gl20.glBlendFunc(sfactor, dfactor);
	}
	
}

