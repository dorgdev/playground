def get_rows():
	res = []
	for i in range(1, 17):
		for j in range(1, 17):
			if i == j:
				continue
			for k in range(1, 17):
				if k in [i,j]:
					continue
				m = 34 - i - j - k
				if m in [i,j,k]:
					continue
				if m > 0 and m < 17:
					sum = (2 ** i + 2 ** j + 2 ** k + 2 ** m) 
					res.append([i,j,k,m, sum])
	return res
	
def filter_rows(mask, rows):
	match = rows
	for i in range(len(mask)):
		if mask[i] == 0:
			continue
		match = [ row for row in rows if (row[i] == mask[i])]
	return match

NO_MASK = [0,0,0,0]

def four(rows, masks):
	rows_i = filter_rows(masks[0], rows)
	rows_j = filter_rows(masks[1], rows)
	rows_k = filter_rows(masks[2], rows)
	rows_m = filter_rows(masks[3], rows)
	for row_i in rows_i:
		crc_i = row_i[4]
		for row_j in rows_j:
			crc_j = crc_i ^ row_j[4]
			for row_k in rows_k:
				crc_k = crc_j ^ row_k[4]
				for row_m in rows_m:
					crc_m = crc_k ^ row_m[4]
					if crc_m == 131070:
						yield [row_i, row_j, row_k, row_m]

def valid_cols(board):
	for i in range(4):
		if (board[0][i] + board[1][i] + board[2][i] + board[3][i]) != 34:
			return False
	return True
	
def valid_diagonals(board):
	if (board[0][0] + board[1][1] + board[2][2] + board[3][3]) != 34:
		return False
	if (board[0][3] + board[1][2] + board[2][1] + board[3][0]) != 34:
		return False
	return True

def valid_board(board):
	return valid_cols(board) and valid_diagonals(board)

def find(rows, masks):
	for board in four(rows, masks):
		if valid_board(board):
			for row in board:
				print row
			print
			print
