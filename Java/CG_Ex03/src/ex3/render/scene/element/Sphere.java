package ex3.render.scene.element;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.math.Ray;
import ex3.math.Vec;
import ex3.render.scene.Hit;
import ex3.utils.Utils;

/**
 * Represents a sphere in the scene (with a center and a radius).
 * 
 * @author dor
 */
public class Sphere extends AbstractSceneElement {

	/** The default texture's center point direction from the center of the sphere */
	public static final Vec DEFAULT_TEXTURE_CENTER_DIR = new Vec(0, 0, 1);
	/** The default texture's top point direction from the center of the sphere */
	public static final Vec DEFAULT_TEXTURE_UP_DIR = new Vec(0, 1, 0);
	
	// The center of the sphere
	public Point3D center;
	// The radius of the sphere (squared for performance)
	public double radiusSquared;
	// A texture covering the sphere
	private BufferedImage texture;
	// The following are the 3 vectors for the texture layering on the sphere
	private Vec textureCenterVec;
	private Vec textureUpVec;
	private Vec textureLeftVec;
	
	/**
	 * Creates a new sphere.
	 */
	public Sphere() {
		super();
	}
	
	/**
	 * Creates a new Sphere from its center point and its radius.
	 * @param center The center of the new Sphere
	 * @param radius The radius of the new Sphere
	 */
	public Sphere(Point3D center, double radius) {
		this.center = center;
		this.radiusSquared = radius * radius;
	}
	
	@Override
	public void init(Map<String, String> attrs) {
		super.init(attrs);
		this.center = Point3D.ParsePoint3D(Utils.getValue(attrs, Utils.SPHERE_CENTER_ATTR));
		double radius = Double.parseDouble(Utils.getValue(attrs, Utils.SPHERE_RADIUS_ATTR));
		this.radiusSquared = radius * radius;
		
		// The sphere's texture
		if (attrs.containsKey(Utils.MTL_TEXTURE_ATTR)) {
			try {
				texture = ImageIO.read(Utils.getResource(attrs.get(Utils.MTL_TEXTURE_ATTR)));
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
			ReadAndAdjustTextureVectors(attrs);
		} else {
			texture = null;
			textureCenterVec = null;
			textureUpVec = null;
			textureLeftVec = null;
		}
	}
		
	@Override
	public Hit intersect(Ray ray) {
		// Search for a "t" satisfying the formula: at^2+bt+c=0 where:
		// a = 1 ; b = 2v.(p0-O) ; c = |p0-O|^2 - r^2
		double a = 1;
		double b = 2 * ray.v.dotProd(ray.p.vecFrom(center));
		double c = ray.p.vecFrom(center).lengthSquared() - radiusSquared;
		// Solve the equation
		double delta = b * b - 4 * a * c;
		if (delta <= 0) {
			// No intersection OR a single intersection (tangent) - shouldn't affect
			return null;
		}
		// Find the closer point to the ray starting point and return it. If it's
		// behind the ray starting position, return null (as no intersection)
		double t = (-b - Math.sqrt(delta)) / (2 * a);

		if (t < 0) {
			return null;
		}
		return createHit(ray.getPoint(t));
	}

	/**
	 * Creates a hit object from a point on the sphere.
	 * @param p The point on the sphere the ray hit
	 * @return An Hit object representing the hit.
	 */
	private Hit createHit(Point3D p) {
		Vec vecFromCenter = p.vecFrom(center);
		vecFromCenter.normalize();
		return new Hit(vecFromCenter, p, this);
	}
	
	@Override
	public RGB mtlDiffuseFactor(Point3D p) {
		// If there's no texture, return the regular diffusion factor
	  if (texture == null) {
	  	return super.mtlDiffuseFactor(p);
	  }
	  // Find the point's corresponding pixel on the texture:
	  // We have 2 angles - alpha (0-180) and beta (0-360) representing the
	  // point intersection. Find those angles and cast their relative factor
	  // on the texture sizes to retrieve the correct color.
	  Vec toPoint = p.vecFrom(center);
	  toPoint.normalize();
	  // Casting the vector to the point on the top vector
	  double topDotProd = toPoint.dotProd(textureUpVec);
	  double alphaFactor = Math.acos(topDotProd) / Math.PI;
	  // Casting the vector to the point on the middle vector
	  Vec down = Vec.negate(textureUpVec);
	  down.scale(topDotProd);
	  toPoint.add(down);
	  toPoint.normalize();
	  double beta = Math.acos(toPoint.dotProd(textureCenterVec));
	  boolean toTheLeft = (toPoint.dotProd(textureLeftVec) >= 0);
	  double betaFactor = 0.5 + (beta / Math.PI * (toTheLeft ? 0.5 : -0.5));
	  // Get the correct pixel from the texture
	  int pixelHeight = (int)((texture.getHeight() - 1) * alphaFactor);
	  int pixelWidth = (int)((texture.getWidth() - 1) * betaFactor);
	  return new RGB(texture.getRGB(pixelWidth, pixelHeight));
	}

	/**
	 * Reads the vectors of the texture's directions (if exist), and adjust them
	 * to be 3 perpendicular vectors.
	 * @param attrs The sphere attribute's map 
	 */
	private void ReadAndAdjustTextureVectors(Map<String, String> attrs) {
		// Center vector
		if (attrs.containsKey(Utils.SPHERE_TEXTURE_CENTER_ATTR)) {
			textureCenterVec = Vec.parseVec(attrs.get(Utils.SPHERE_TEXTURE_CENTER_ATTR));
		} else {
			textureCenterVec = DEFAULT_TEXTURE_CENTER_DIR;
		}
		textureCenterVec.normalize();
		// Top vector
		if (attrs.containsKey(Utils.SPHERE_TEXTURE_UP_ATTR)) {
			textureUpVec = Vec.parseVec(attrs.get(Utils.SPHERE_TEXTURE_UP_ATTR));
		} else {
			textureUpVec = DEFAULT_TEXTURE_UP_DIR;
		}
		// Make sure both are orthogonal. fix if not:
		textureUpVec = Utils.fixOrthogonal(textureCenterVec, textureUpVec);
		if (textureUpVec == null) {
			throw new IllegalArgumentException("Texture's direction and up vector " +
					"shouldn't be linearly dependent!");
		}
		textureUpVec.normalize();
		// Left vector (According to right hand rule...)
		textureLeftVec = Vec.crossProd(textureUpVec, textureCenterVec);
		textureLeftVec.normalize();
	}
}
