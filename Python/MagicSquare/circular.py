def circular(start, end):
	dict = {}
	queue = [start]
	while (len(queue) > 0):
		elem = queue[0]
		node = elem[0]
		queue = queue[1:]
		links = elem[1] + node
		node1 = [(node[0] + 1) % 36, (node[1] + 2) % 36, (node[2] - 3) % 36]
		node2 = [(node[0] - 1) % 36, (node[1] - 2) % 36, (node[2] + 3) % 36]
		node3 = [(node[0] + 2) % 36, (node[1] + 3) % 36, (node[2] - 1) % 36]
		node4 = [(node[0] - 2) % 36, (node[1] - 3) % 36, (node[2] + 1) % 36]
		node5 = [(node[0] + 3) % 36, (node[1] + 1) % 36, (node[2] - 2) % 36]
		node6 = [(node[0] - 3) % 36, (node[1] - 1) % 36, (node[2] + 2) % 36]
		nodes = [node1, node2, node3, node4, node5, node6]
		for nodei in nodes:
			if nodei == end:
				print links
				return
			if dict.has_key(nodei):
				continue
			queue.append([nodei, links])
			dict[nodei] = True

