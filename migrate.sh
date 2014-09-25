# this file was used to import the SVN history into git.
svn2git https://svn.mpi.nl/LAT --username petwit --authors ../users.txt --notags --nobranches --trunk trunk/src/java/mpi/linorg;
git remote add origin https://github.com/TheLanguageArchive/Arbil.git;
#git push --all -u;
# push the local master into the existing Arbil repository as a new branch "linorg"
git push origin master:linorg
# please note that new clones taken from GitHub will not have the svn information required to 
# pull new changes from svn to git. So it is recommended to keep the original that you pushed
# to GitHub if you wish to updates from SVN. If this is not possible you can always pull whole
# lot from SVN again with this script into a fresh Git repostitory.
