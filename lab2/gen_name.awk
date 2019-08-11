function gen_id(base, range) {
	return int(base + rand() * (range+1))
}

BEGIN { # simply maps first name with last name in sequence
	num_stu = 0;
	while (getline < firstname)
	{
		name[num_stu] = $0;
		num_stu++;
	}
	close(firstname);
	srand(); 
	il = 0;
	while (getline < lastname && il < num_stu)
	{
		name[il] = sprintf("%s %s,%010d", name[il], $0, gen_id(base, range));
		il++;
	}
	il = 0;
	while (il++ < record)
	{
		# start generate gradebook.csv
		printf "%s,%d\n", name[gen_id(0, num_stu-1)], gen_id(0, 99);
	}
}
