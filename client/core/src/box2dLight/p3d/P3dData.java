package box2dLight.p3d;

import com.badlogic.gdx.physics.box2d.Fixture;

/**
 * Data container for linking fixtures with pseudo3d light engine
 * 
 * @author rinold
 */
public class P3dData {
	
	public Object userData;
	
	public float height;
	public boolean ignoreDirectional;
	
	public P3dData(float h) {
		height = h;
	}
	
	public P3dData(Object data, float h) {
		height = h;
		userData = data;
	}

	public boolean isIgnoreDirectional() {
		return ignoreDirectional;
	}

	public void setIgnoreDirectional(boolean ignoreDirectional) {
		this.ignoreDirectional = ignoreDirectional;
	}

	public float getLimit(float distance, float lightHeight, float lightRange) {
		float l;
		if (lightHeight > height) {
			l = distance * height / (lightHeight - height);
			float diff = lightRange - distance;
			if (l > diff) l = diff;
		} else {
			l = lightRange - distance;
		}
		
		return l > 0 ? l : 0f;
	}
	
	public static Object getUserData(Fixture fixture) {
		Object data = fixture.getUserData();
		if (data instanceof P3dData) {
			return ((P3dData)data).userData;
		} else {
			return data;
		}
	}
	
}
