import math

def mul_mat(A, B):
	res = []
	for i in range(len(A)):
		vec = []
		for j in range(len(B[0])):
			val = 0.0
			for k in range(len(A[0])):
				val += 1.0 * A[i][k] * B[k][j]
			vec.append(val)
		res.append(vec)
	return res

def mul_mats(l):
	res = [] + l[0]
	for mat in l[1:]:
		res = mul_mat(res, mat)
	return res

def m(l):
	return mul_mats(l)

def mul_vec(u, v):
	res = []
	res.append(u[1] * v[2] - u[2] * v[1])
	res.append(u[2] * v[0] - u[0] * v[2])
	res.append(u[0] * v[1] - u[1] * v[0])
	return res
	
def normalize(v):
	l = 0.0
	for val in v:
		l += val ** 2
	l = math.sqrt(l)
	res = []
	for val in v:
		res.append(val / l)
	return res

def rotate(axis, angle, ccw = True):
	rads = angle * math.pi / 180.0
	c = math.cos(rads)
	s = math.sin(rads)
	res = [[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]
	minus = [0, 0]
	if axis == 'x':
		res[1][1] = c
		res[2][2] = c
		res[1][2] = s
		res[2][1] = s
		if ccw:
			minus = [1, 2]
		else:
			minus = [2, 1]
	elif axis == 'y':
		res[0][0] = c
		res[2][2] = c
		res[2][0] = s
		res[0][2] = s
		if ccw:
			minus = [0, 2]
		else:
			minus = [2, 0]
	elif axis == 'z':
		res[0][0] = c
		res[1][1] = c
		res[1][0] = s
		res[0][1] = s
		if ccw:
			minus = [0, 1]
		else:
			minus = [1, 0]
	else:
		return []
	res[minus[0]][minus[1]] *= -1
	return res

def rotate_x(angle, ccw):
	return rotate('x', angle, ccw)

def rotate_y(angle, ccw):
	return rotate('y', angle, ccw)

def rotate_z(angle, ccw):
	return rotate('z', angle, ccw)

def scale(x, y, z):
	return [[x, 0, 0, 0], [0, y, 0, 0], [0, 0, z, 0], [0, 0, 0, 1]]

def move(x, y, z):
	return [[1, 0, 0, x], [0, 1, 0, y], [0, 0, 1, z], [0, 0, 0, 1]]
	
def translate(x, y, z):
	return move(x, y, z)

def mirror(axis):
	res = [[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]
	cell = -1
	if axis == 'x':
		cell = 0
	elif axis == 'y':
		cell = 1
	elif axis == 'z':
		cell = 2
	else:
		return []
	res[cell][cell] = -1
	return res

def mirror_x():
	return mirror('x')

def mirror_y():
	return mirror('y')

def mirror_z():
	return mirror('z')

def print_mat(m):
	for line in m:
		print line

def p(m):
	print_mat(m)

def cross(u, v):
	return [u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0]]

def to_axis(vec):
	w = normalize(vec)
	v = normalize(cross(w, [0,0,1]))
	u = cross(v,w)
	return [[u[0], u[1], u[2], 0], [v[0], v[1], v[2], 0], [w[0], w[1], w[2], 0], [0, 0, 0, 1]]

def from_axis(vec):
	w = normalize(vec)
	v = normalize(cross(w, [0,0,1]))
	u = cross(v,w)
	return [[u[0], v[0], w[0], 0], [u[1], v[1], w[1], 0], [u[2], v[2], w[2], 0], [0, 0, 0, 1]]

def vec(x,y,z):
	return [x, y, z, 1]
	
def ver(x,y,z):
	return [[x], [y], [z], [1]]

def from_ver(v):
	return [v[0][0], v[1][0], v[2][0], v[3][0]]

def minus(v1, v2, s):
	res = []
	for i in range(len(v1)):
		res.append(v1[i] - s * v2[i])
	return res

def round(x):
	x_int = int(x)
	d = x - x_int
	if (d > 0.5):
		return x_int + 1
	elif (d <= 0.5 and d >= -0.5):
		return x_int
	else:
		return x_int - 1

def r(mat):
	res = []
	for line in mat:
		newline = []
		for col in line:
			newline.append(round(col))
		res.append(newline)
	return res
