def print_mat(A):
	n = len(A)
	for i in range(n):
		for j in range(n):
			num = A[i][j]
			s = " "
			if (num < 0):
				s = "-"
			num = abs(num)
			s += str(num)
			if (num < 100):
				s += " "
			if (num < 10):
				s += " "
				
			print s,
		print
		
def mul_mat(A,B):
	C = []
	n = len(A)
	for i in range(n):
		C.append([])
		for j in range(n):
			sum = 0
			for k in range(n):	
				sum += A[i][k] * B[k][j]
			C[i].append(sum)
	return C
	
def add_mat(A,B):
	C = []
	n = len(A)
	for i in range(n):
		C.append([])
		for j in range(n):
			C[i].append(A[i][j] + B[i][j])
	return C
	
def t_mat(A):
	C = []
	n = len(A)
	for i in range(n):
		C.append([])
		for j in range(n):
			C[i].append(A[j][i])
	return C

def mulc_mat(a,A):
	n = len(A)
	C = []
	for i in range(n):
		C.append([])
		for j in range(n):
			C[i].append(a * A[i][j])
	return C

def vec_to_mat(x):
    C = []
    n = len(x)
    for i in range(n):
        y = [x[i]]
        for j in range(n-1):
            y.append(0)
        C.append(y)
    return C

