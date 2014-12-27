# Input:
#   A list with (BASE * N + 1) values. N valeus appear BASE time,
#   and 1 value appears only once in the list.
# Output:
#   The single element that appears only once in the list.

def to_base(n, b):
	"""
	Transforms the number N into a list of base B.
	E.g.: to_base(13, 2) => [1, 1, 0, 1]
	E.g.: to_base(13, 3) => [1, 1, 1]
	"""
	if n == 0:
		return [0]
	res = []
	while n > 0:
		res.insert(0, n % b)
		n = n / b
	return res

def from_base(l, b):
	"""
	Returns the base10 representation of the number given as a list L of
	digits in base B.
	E.g.: from_base([1, 1, 0, 1], 2) => 13
	E.g.: from_base([0, 1, 1, 1], 3) => 13
	"""
	return reduce(lambda x, y: x * b + y, l)

def add_same_base(x, y):
	"""
	Adds 2 lists, X and Y, into a single list of their local sums.
	Note: Prepend 0's if the lists do not share the same length.
	E.g.: add_same_base([1, 2, 3], [5, 5, 5]) => [6, 7, 8]
	E.g.: add_same_base([1, 2], [6, 6, 6]) => [6, 7, 8]
	"""
	[longer, shorter] = [x, y] if len(x) > len(y) else [y, x]
	n = len(longer)
	len_shorter = len(shorter)
	while (len_shorter < n):
		shorter.insert(0, 0)
		len_shorter += 1
	return map(lambda a, b: a + b, longer, shorter)

def find_the_one(l, b):
	"""
	See file's top comment.
	Finds the single element in a list L of n elements appearing B times and one
	element appearing only once.
	E.g.: find_the_one([1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 6, 6, 4, 3, 2, 1], 3) => 5
	E.g.: find_the_one([5, 3, 6, 1, 8, 4, 2, 7, 5, 2, 4, 7, 3, 1, 8], 2) => 6
	"""
	res = []
	for n in l:
		res = add_same_base(res, to_base(n, b))
	for i in range(len(res)):
		res[i] = res[i] % b
	return from_base(res, b)
	
	