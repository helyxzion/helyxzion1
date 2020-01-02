


package ONE;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.border.LineBorder;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;

public class ANVIL8 {

	private JFrame frmHelyxzionAnvil;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ANVIL window = new ANVIL();
					window.frmHelyxzionAnvil.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ANVIL() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmHelyxzionAnvil = new JFrame("HELP");
		frmHelyxzionAnvil.setTitle("HELYXZION ANVIL 4.0");
		frmHelyxzionAnvil.getContentPane().setFont(new Font("Times New Roman", Font.BOLD, 14));
		frmHelyxzionAnvil.getContentPane().setForeground(new Color(0, 0, 0));
		frmHelyxzionAnvil.setBounds(100, 100, 900, 600);
		frmHelyxzionAnvil.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.RED, 3));
		panel.setBackground(Color.WHITE);
		frmHelyxzionAnvil.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		frmHelyxzionAnvil.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{panel}));
		System.loadLibrary(arg0);
	}
	use CGI qw/:standard/;
	use DBI;

	$q = new CGI;

	$CGI	= "prodemo.cgi";
	$VCGI = "prodemoview.cgi";
	$USER_IDENT = 1;
	$SWF	= "betaviewer.swf?dataSource=".$VCGI."&selectorSource=".$VCGI;
	$UIFILE = "hlxuif.html";
	$multi	= "";
	#$multi	 = " ENCTYPE=\"multipart/form-data\" ";
	$HEADER	= 0;
	$COOKIENAME = "charles";
	$THISDOMAIN = "helyxzion.net";

	my $myaction = $q->param('myaction') || "";
	#print STDERR "\n\n\n\n\nmyaction = $myaction\n";

	$getOrg = $q->param('getOrg') || "";
	$findlocus = $q->param('findlocus') || "";
	$show = $q->param('show') || "";
	$view = $q->param('view') || "";


	if ($myaction eq "logout") {
		&logout;
		exit;
	}

	#print $q->header();

	##### REGISTRATION #####
	# registered and check_login will print cookie header if successful
	if (!&registered(0)) {
		if (!&check_login(1)) {
			print STDERR "Did not pass Check Login - printing Login Page\n";
			&printLoginPage;
		}
	} else {
		print STDERR "Registered returned ![".&registered(0)."]\n";
	}

	#$today		= &ParseDate("today");
	#my @parms = $q->param;
	#foreach $parm (@parms) {
		#print STDERR "q->param($parm) = ".$q->param($parm)."<BR>\n";
	#}
	#print "<BR>\n";

	###########################################################################
	##### ACTION HANDLER #####


	if ($myaction eq "" || $myaction eq "getOrgs") {
		&getCustomData();
	} elsif ($myaction eq "userlogin") {
		&getCustomData();
	} elsif ($myaction eq "getData") {
		&getData;
	} elsif ($myaction eq "addrecord") {
		&getNewGene;
	} elsif ($myaction eq "delrecord") {
		&printDeleteRecs;
	} elsif ($myaction eq "addDBRec") {
		&addDBRec;
	} elsif ($myaction eq "delDBRec") {
		&delDBRec;
	} elsif ($myaction eq "logout") {
		&logout;
		exit;
	} else {
		&printLoginPage;
	}

	print STDERR "\nGot to main exit.\n\n";
	exit;


	##### SUBROUTINES

	sub getData
	{
		my $locus = $q->param('locus') || 1;
		my $organism = $q->param('organism');
		my $PHASE = $q->param('phase') || 0;
		my $page = $q->param('page') || 1;
		my $direction = $q->param('direction') || 0;

		my $dba = &dbconnect;
		my $sqlStr0 = "SELECT Locus, Origin from userorigin where OriginID = '$locus' AND UserId = '$USER_IDENT'";
		my $stb = $dba->prepare($sqlStr0)   or die "Couldn't prepare statement: ".$dba->errstr;
		$stb->execute                       or die "Couldn't execute statement: ".$stb->errstr;
		@originrecord = $stb->fetchrow();
		my $loc = $originrecord[0];
		my $org = $originrecord[1];

		if ($PHASE == 1) {
			$org = "..".$org;
		} elsif ($PHASE == 2) {
			$org = ".".$org;
		}

		#print STDERR "Translating:\n$org\n";

		$loc =~ s/([&=])//g;#\\$1/;
		$loc =~ s/\|/-/g;#\\$1/;
		$org  =~ s/([&=])//g;#\\$1/;

		print STDERR "\nSending name = $loc \n\n";
		#$TRANS = "NAME=".$loc;
		$TRANS = "NAME=ForFranz";
		#$TRANS .= "&MAXDP=".sprintf("%d",(length($org)/3));
		$TRANS .= "&TRANS=".&translateHelyxzion($org);
		print $q->header('application/x-www-form-urlencoded');
		print $TRANS;
		print STDERR "Sending:\n$TRANS\n";
		exit;
	}

	sub translateHelyxzion
	{
		$codons = shift();

		my @helx;
		my $transstring = "";
		while ($codons =~ s/^(...)//) {
			my $codon = $1;
			#print "Converting $codon <BR>\n";
			# !"#$c{~()*a,-./0123456789:;<}>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[|]^_b
			push @helx, &convertLetter($codon);
		}
		#print STDERR "[$codons] of length(".length($codons).") is leftover from the translation.\n";
		if (length($codons) > 0) {
			for (my $l=0; $l < (3-length($codons)); $l++) {
				$codons .= ".";
			}
			push @helx, &convertLetter($codons);
		}
		for (my $k=0; $k <= $#helx; $k++) {
			$paramstring .= $helx[$k];
		}
		return $paramstring;
	}

	sub convertLetter
	{
		my $codon = shift();
		$codon =~ tr/a-z/A-Z/;
		for ($codon) {
			# !"#$c{~()*a,-./0123456789:;<}>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[|]^_b
			/AAA/	&& do { return '!'; };

			/AAC/	&& do { return '"'; };
			/ACA/	&& do { return '#'; };
			/CAA/	&& do { return '$'; };

			/AAG/	&& do { return 'c'; };
			/AGA/	&& do { return '{'; };
			/GAA/	&& do { return '~'; };
			/CCA/	&& do { return '('; };
			/CAC/	&& do { return ')'; };
			/ACC/	&& do { return '*'; };

			/AAT/	&& do { return 'a'; };
			/ATA/	&& do { return ','; };
			/TAA/	&& do { return '-'; };
			/CCC/	&& do { return '.'; };
			/CAG/	&& do { return '/'; };
			/ACG/	&& do { return '0'; };
			/AGC/	&& do { return '1'; };
			/GAC/	&& do { return '2'; };
			/GCA/	&& do { return '3'; };
			/CGA/	&& do { return '4'; };

			/GGA/	&& do { return '5'; };
			/GAG/	&& do { return '6'; };
			/AGG/	&& do { return '7'; };
			/CCG/	&& do { return '8'; };
			/CAT/	&& do { return '9'; };
			/ACT/	&& do { return ':'; };
			/TCA/	&& do { return ';'; };
			/CTA/	&& do { return '<'; };
			/CGC/	&& do { return '}'; };
			/GCC/	&& do { return '>'; };
			/TAC/	&& do { return '?'; };
			/ATC/	&& do { return '@'; };

			/TTT/	&& do { return 'A'; };

			/TTG/	&& do { return 'B'; };
			/TGT/	&& do { return 'C'; };
			/GTT/	&& do { return 'D'; };

			/TTC/	&& do { return 'E'; };
			/TCT/	&& do { return 'F'; };
			/CTT/	&& do { return 'G'; };
			/GGT/	&& do { return 'H'; };
			/GTG/	&& do { return 'I'; };
			/TGG/	&& do { return 'J'; };

			/TTA/	&& do { return 'K'; };
			/TAT/	&& do { return 'L'; };
			/ATT/	&& do { return 'M'; };
			/GGG/	&& do { return 'N'; };
			/GTC/	&& do { return 'O'; };
			/TGC/	&& do { return 'P'; };
			/TCG/	&& do { return 'Q'; };
			/CTG/	&& do { return 'R'; };
			/CGT/	&& do { return 'S'; };
			/GCT/	&& do { return 'T'; };

			/CCT/	&& do { return 'U'; };
			/CTC/	&& do { return 'V'; };
			/TCC/	&& do { return 'W'; };
			/GGC/	&& do { return 'X'; };
			/GTA/	&& do { return 'Y'; };
			/TGA/	&& do { return 'Z'; };
			/AGT/	&& do { return '['; };
			/GAT/	&& do { return '|'; };
			/GCG/	&& do { return ']'; };
			/CGG/	&& do { return '^'; };
			/ATG/	&& do { return '_'; };
			/TAG/	&& do { return 'b'; };

			/\.+/ && do { print STDERR "[$codon] is hanger.\n"; return 'y'.$codon; };
			/[^ACTG]/i && do { print STDERR "[$codon] is ambig.\n"; return 'x'.$codon; };
		}
	}



	sub url_encode
	{
		my $str = shift();
		$str =~ s/([^\$])/uc sprintf("%%%02x",ord($1))/eg;
		$str =~ s/([^a-zA-Z0-9_\-.])/uc sprintf("%%%02x",ord($1))/eg;
		return $str;
	}

	sub url_decode
	{
		my $str = shift();
		$str =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;
		return $str;
	}

	sub delDBRec
	{
		my $dba = &dbconnect;
		my $ORGNID = $q->param('OriginID') || "";
		if ($ORGNID eq "") {
			&printError("No Origin ID given");
		}
		$ORGNID =~ s/[^0-9]//g;
		my $sqlStr0 = "DELETE FROM userorigin WHERE OriginID='$ORGNID'";
		$stb = $dba->prepare($sqlStr0);
		$stb->execute or &printError("Couldn't execute statement: ".$stb->errstr);
		$stb->finish;
		$dba->disconnect;
		&getCustomData;
		#my $linktext = "<A HREF=\"$CGI?myaction=addrecord\">Add a new gene</A><BR>\n";
		#$linktext .= "<A HREF=\"$CGI?myaction=delrecord\">Delete gene</A><BR>\n";
		#&printHlxHeader("Custom Data","Custom Data","",$linktext);
		#print "Record Deleted.<BR><BR>\n";
		#&printLogoutButton;
		#&printHlxFooter;
	}

	sub addDBRec
	{
		my $dba = &dbconnect;
		my $uid = $q->param('uid') || 5;	# Default beta tester = 5
		my $NAME = $q->param('geneName') || "Unknown";
		my $ORGN = $q->param('codons') || "";
		if ($ORGN eq "") {
			&printError("No codons given");
		}
		$ORGN =~ tr/A-Z/a-z/;
		$ORGN =~ s/[^a-z]//g;
		$NAME =~ s/'/\\'/g;
		$NAME =~ s/\|/-/g;
		my $sqlStr0 = "INSERT INTO userorigin VALUES ('','$uid','$NAME','$ORGN')";
		$stb = $dba->prepare($sqlStr0);
		$stb->execute or &printError("Couldn't execute statement: ".$stb->errstr);
		$stb->finish;
		$dba->disconnect;
		&getCustomData;
	}

	sub getNewGene
	{
		&printHlxHeader("Enter Custom Data","Enter Custom Data","","");
		print <<endOfFormPage1;
	<FONT FACE="Arial, sans-serif">
	<BR>
	<CENTER>
	<FORM NAME="pasteform" ACTION="$CGI" METHOD="POST">
	<INPUT TYPE="HIDDEN" NAME="myaction" VALUE="addDBRec">
	<TABLE BGCOLOR="#CCCCCC" BORDER="0" CELLPADDING="6">
	 <TR>
	  <TD STYLE="color:#FFFFFF; background-color:#666666" COLSPAN="2">
	Enter new gene for custom data<BR>
	  </TD>
	 </TR>
	 <TR>
	  <TD STYLE="background-color:#CCCCCC" COLSPAN="2">
	<BR>
	You may cut and paste [ACTG] for translation.<BR>
	<BR>
	<A TARGET="NCBI" HREF="http://ncbi.nlm.nih.gov/mapview/">VISIT NCBI in new window</A><BR>
	<BR>
	&nbsp;&nbsp;Locus:<BR>
	&nbsp;&nbsp;<INPUT TYPE="TEXT" NAME="geneName" SIZE="40">
	<BR>
	&nbsp;&nbsp;Nucleic Acid String:<BR>
	&nbsp;&nbsp;<TEXTAREA NAME="codons" WRAP="virtual" ROWS="20" COLS="65"></TEXTAREA>
	  </TD>
	 </TR>
	 <TR>
	  <TD>
	Visible by:<BR>
	<SELECT NAME="uid">
	endOfFormPage1
		my $dba = DBI->connect('DBI:mysql:usertable;mysql_socket=/private/var/mysql/mysql.sock','hlx','user') or &printError("Couldn't connect to database: ".DBI->errstr);
		my $sqlStr0 = "SELECT UID, description from users";
		$stb = $dba->prepare($sqlStr0);
		$stb->execute or &printError("Couldn't execute statement: ".$stb->errstr);
		my @accts;
		while (@accts = $stb->fetchrow()) {
			print "<OPTION VALUE=\"$accts[0]\">$accts[1]</OPTION>\n";
		}
		print <<endOfFormPage2;
	</SELECT>
	  </TD>
	 </TR>
	 <TR>
	  <TD ALIGN="RIGHT">
	<INPUT TYPE="SUBMIT" VALUE="Submit Record"><BR>
	<A HREF="$CGI">Cancel</A>
	  </TD>
	 </TR>
	</TABLE>
	</FORM>
	<BR>
	endOfFormPage2
		&printLogoutButton;
		&printHlxFooter;
	}

	sub printDeleteRecs
	{
		my $orgName = $organism;
		print STDERR "ORGANISM = $orgName\n";
		$orgName = &url_decode($orgName);
		$orgNameEnc = &url_encode($orgName);
		my $numCols = 3;
		my $dba = &dbconnect;
		my ($sta,$stb,$numOfLocs,$numOfPages,$rowsPerCol,$sqlStr0,$numRows,$instruct);
		my $i = 0;
		my $start = ($page - 1) * $ataTime;
		my $sqlStr0 = "SELECT OriginID, Locus from userorigin WHERE UserId = '".$USER_IDENT."' ORDER BY OriginID";
		$stb = $dba->prepare($sqlStr0);
		$stb->execute or &printError("Couldn't execute statement: ".$stb->errstr);
		$numRows = $stb->rows;
		my $linktext = "";
		#$linktext = "<A HREF=\"$CGI?myaction=addrecord\">Add a new gene</A><BR>\n";
		#$linktext .= "<A HREF=\"$CGI?myaction=delrecord\">Delete gene</A><BR>\n";
		$linktext .= "<A HREF=\"$CGI\">View a gene</A><BR>\n";
		&printHlxHeader("Custom Data","Custom Data","",$linktext);
		print "<B>Please choose a record number to delete:</B><BR>\n";
		print "<FORM NAME=\"deleteForm\" ACTION=\"$CGI\" METHOD=\"POST\">\n";
		print "<INPUT TYPE=\"HIDDEN\" NAME=\"myaction\" VALUE=\"delDBRec\">\n";
		print "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" WIDTH=\"100%\">\n";
		print " <TR>\n";
		print "  <TD>\n";
		if ($numRows > 0) {
			while (@found = $stb->fetchrow()) {
				print "<INPUT TYPE=\"SUBMIT\" NAME=\"OriginID\" VALUE=\"$found[0]\"> $found[1]<BR>\n";
			}
		} else {
			print "  <TD>Found no results.</TD>\n";
		}
		print "  </TD>\n";
		print " </TR>\n";
		print "</TABLE>\n";
		print "</FORM>\n";
		print $pageText;
		$stb->finish;
		$dba->disconnect;
		&printLogoutButton;
		&printHlxFooter;
	}

	sub getCustomData
	{
		my $orgName = $organism;
		print STDERR "ORGANISM = $orgName\n";
		$orgName = &url_decode($orgName);
		$orgNameEnc = &url_encode($orgName);
		my $numCols = 3;
		my $dba = &dbconnect;
		my ($sta,$stb,$numOfLocs,$numOfPages,$rowsPerCol,$sqlStr0,$numRows,$instruct);
		my $i = 0;
		my $start = ($page - 1) * $ataTime;
		my $sqlStr0 = "SELECT OriginID, Locus from userorigin WHERE UserId = '".$USER_IDENT."' ORDER BY OriginID";
		$stb = $dba->prepare($sqlStr0);
		$stb->execute or &printError("Couldn't execute statement: ".$stb->errstr);
		$numRows = $stb->rows;
		my $linktext = "";
		#if ($USER_IDENT == 3 || $USER_IDENT == 1) {
		#if ($USER_IDENT == 3 || $USER_IDENT == 1 || $USER_IDENT == 5) {
			$linktext = "<A HREF=\"$CGI?myaction=addrecord\">Add a new gene</A><BR>\n";
			$linktext .= "<A HREF=\"$CGI?myaction=delrecord\">Delete gene</A><BR>\n";
		#}
		$linktext .= "<A HREF=\"$CGI\">View a gene</A><BR>\n";
		&printHlxHeader("Custom Data","Custom Data","",$linktext);
		print "<FONT SIZE=-1><B>Please note this current version will not automatically load sequence.  When Pro Viewer loads, select 'Load new data set' from one of the Graph Control panes.</B><BR><BR></FONT>";
		print "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" WIDTH=\"100%\">\n";
		print " <TR>\n";
		print "  <TD>\n";
		if ($numRows > 0) {
			while (@found = $stb->fetchrow()) {
				print "<A HREF=\"$SWF\">$found[1]</A><BR>\n";
			}
		} else {
			print "  <TD>Found no results.</TD>\n";
		}
		print "  </TD>\n";
		print " </TR>\n";
		print "</TABLE>\n";
		print $pageText;
		$stb->finish;
		$dba->disconnect;
		&printLogoutButton;
		&printHlxFooter;
	}

	sub printHlxHeader
	{
		my $str1 = shift() || ""; # Title
		my $str2 = shift() || ""; # Page Header
		my $str3 = shift() || ""; # Scripts
		my $str4 = shift() || ""; # LeftBar

		if (!$HEADER) {
			print $q->header();
			print STDERR "Manually printed header without cookie.<BR>\n";
		}

	$thescript = <<endOfScript1a;

	function getCookie(name) {
	  var chips = document.cookie.split("; ");
	  for (i=0; i<chips.length; i++) {
	    chocolate = chips[i].split("=");
	    if (chocolate[0] == name)
	      return unescape(chocolate[1]);
	  }
	  return "";
	}


	function setCookie(name,value) {
	  var today = new Date();
	  var domain = '$DOMAIN';
	  var path = "/";
	  var secure = 0;
	  var expires = new Date(today.getTime() + 1 *4*3600000); // 4/24 of a day
	  var myCookie = name + "=" + escape(hxd) +
	      ((expires) ? "; expires=" + expires.toGMTString() : "") +
	      ((path) ? "; path=" + path : "") +
	      //((domain) ? "; domain=" + domain : "") +
	      ((secure) ? "; secure" : "");
	  document.cookie = myCookie;
	}
	endOfScript1a

		open(UIFILE,"$UIFILE") || die("Couldn't open $UIFILE");
		my $continue = 1;
		my $line;
		$str3 .= "\n".$thescript;
		while ($continue == 1) {
			if ($line = <UIFILE>) {
				$line =~ s/<TITLE>(.*)<\/TITLE>/<TITLE>$1 - $str1<\/TITLE>/i;
				$line =~ s/\/\/HLXSCRIPT/$str3/i;
				$line =~ s/^(.*)<!--HLXPAGEHEADER-->/$1$str2/i;
				$line =~ s/^(.*)<!--HLXLEFTBAR-->/$1$str4/i;
				if ($line =~ s/^(.*)<!--HLXINCLUDE-->/$1/i) {
					$continue = 0;
				}
				print $line;
			} else {
				$continue = 0;
			}
		}
		close(UIFILE);
	}

	sub printLogoutButton
	{
		print "<FORM NAME=\"logout\" ACTION=\"$CGI\" METHOD=\"POST\">\n";
		print "<INPUT TYPE=\"HIDDEN\" NAME=\"myaction\" VALUE=\"logout\">\n";
		print "<INPUT TYPE=\"submit\" VALUE=\"Log Out\">\n";
		print "</FORM>\n";
	}

	sub printHlxFooter
	{
		open(UIFILE,"$UIFILE") || die("Couldn't open $UIFILE");
		my $waitingForTag = 1;
		my $line;
		while ($waitingForTag) {
			if ($line = <UIFILE>) {
				if ($line =~ s/.*<!--HLXINCLUDE-->(.*)$/$1/i) {
					$waitingForTag = 0;
				}
			} else {
				$waitingForTag = 0;
			}
		}
		print $line;
		while ($line = <UIFILE>) {
			print $line;
		}
		close(UIFILE);
		exit;
	}

	sub printSelectorPage
	{
		&printHlxHeader("Selector","Selector",'',"Welcome,<BR>\n<BR>\nThis is a simple selector to select which gene you wish to view.  This is a temporary selector.  Soon, expect graphical chromosome navigation, search functionality, and custom databases.");
		print <<endOfSelectorPage1;
	This is the selector page.
	endOfSelectorPage1
		&printLogoutButton;
		&printHlxFooter;
	}


	sub printCancelPage
	{
		&printHlxHeader("Action has been cancelled.<BR><BR>","Cancel");
		print <<endOfCancelPage1;
	User ID = $UserID for reference.
	<A HREF="$CGI?action=mainmenu">Back to Helyxzion.com</A><BR>
	endOfCancelPage1
		&printHlxFooter;
	}

	sub printUserMenu
	{
		my $mesg = shift()."<BR>\n" || "";
		&printHlxHeader("Helyxzion User - Main Menu","Helyxzion User<BR>Main Menu","",
						"Welcome to the Main Menu");
		print <<endOfMainMenu1;
	$mesg
	Please select one of the following options:<BR>
	<BR>
	<A HREF="$CGI?myaction=userviewtour">View Helyxzion Product Tour</A><BR>
	<A HREF="$CGI?myaction=userbuylicense">Purchase a License</A><BR>
	<A HREF="$CGI?myaction=userviewer">Log In to Helyxzion Viewer</A><BR>
	Request Custom Data Preparation</A><BR>
	<A HREF="$CGI?myaction=logout">Log Off</A><BR>
	<BR>
	endOfMainMenu1
		&printHlxFooter;
	}

	sub printLoginPage
	{
		my $UserName = $q->param('UserName') || "";
		my $Password = "";
		my $mesg = shift() || "";
		&printHlxHeader("User Login","User Login","",
						"Please login to the Helyxzion system with your User Name and Password.");
		print <<endOfTourPage1;
	$mesg
	<P STYLE="font-size:8pt;" CLASS="blue"><A TARGET="flash" HREF="http://www.macromedia.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash">Click here</A> for the latest Flash&trade; plugin.</P>
	<TABLE BORDER="0" WIDTH="100%" CELLPADDING="5" CELLSPACING="0">
	  <TR>
	    <TD VALIGN="TOP">
	<H4>Existing Users:</H4>
	<FORM NAME="loginform" ACTION="$CGI" METHOD="POST">
	User Name:<BR>
	<INPUT TYPE="TEXT" NAME="UserName" VALUE="$UserName" SIZE="35"><BR>
	Password:<BR>
	<INPUT TYPE="PASSWORD" NAME="Password" VALUE="$Password" SIZE="35"><BR>
	<INPUT TYPE="HIDDEN" NAME="myaction" VALUE="userlogin">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="User Login">
	</FORM>
	    </TD>
	  </TR>
	</TABLE>
	<P CLASS="smaller" ALIGN="CENTER">
	<A HREF="mailto:support\@helyxzion.com">Contact our support staff</A>, if you need assistance.<BR>
	</P>
	<BR>
	endOfTourPage1
		&printHlxFooter;
	}

	sub printTourPage
	{
		my $mesg = shift() || "";
		my $tourscript = <<endOfTourScriptB;
	function verifyFields() {
	    var errorText = "";
	    with (document.account_form) {
	        if (UserName.value == "")
	            errorText += "Your User Name must be filled out.\\n";
	        if (Password.value == "")
	            errorText += "You must create a password.\\n";
	        else if (Password.value.match(/[^a-zA-Z0-9]/))
	            errorText += "You must create an alphanumeric password.\\n";
	        else if (Password.value.length < 4)
	            errorText += "Your password must be 4 or more characters in length.\\n";
	        else if (Password.value != ConfirmPassword.value)
	            errorText += "Your Passwords did not match.\\n";
			if (iagreetoterms[0].checked != true)
				errorText += "You must agree to terms and conditions.\\n";
	    }
	    if (errorText != "") {
	        alert(errorText);
	        return false;
	    }
	    return true;
	}

	function cancelForm() {
		document.location = "$CGI";
	}
	endOfTourScriptB

		my $FirstName = $q->param('FirstName');
		my $LastName = $q->param('LastName');
		&printHlxHeader("New User - View Tour","New User - View Tour",$tourscript,
						"<B>Welcome, $FirstName $LastName</B><BR>\n<BR>\nTo view the tour, please choose a User Name and Password, and agree to the terms below.  Then, click the 'Next' button.");
		my @SEL;
		### Insert UI Here
		print <<endOfResponse1;
	<FONT COLOR="RED"><B>$mesg</B></FONT>
	<DIV CLASS="pageGrey" ALIGN="CENTER">
	<BR>
	<FORM NAME="account_form" ACTION="$CGI" METHOD="POST" onSubmit="return verifyFields()">
	<TABLE BORDER="0" CELLPADDING="2" CELLSPACING="0">
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>User Name:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="UserName" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Password:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="PASSWORD" NAME="Password" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Confirm Password:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="PASSWORD" NAME="ConfirmPassword" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD COLSPAN="2" ALIGN="CENTER" VALIGN="TOP">
	Terms and Conditions<BR>
	<TEXTAREA ROWS="5" COLS="40">Small NDA Here</TEXTAREA>
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	&nbsp;
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="RADIO" NAME="iagreetoterms" VALUE="yes"> I Agree<BR>
	<INPUT TYPE="RADIO" NAME="iagreetoterms" VALUE="no"> I Disagree<BR>
	    </TD>
	  </TR>
	  <TR>
	    <TD COLSPAN="2" ALIGN="RIGHT" VALIGN="TOP">
	<INPUT TYPE="HIDDEN" NAME="UserId" VALUE="$UserId">
	<INPUT TYPE="HIDDEN" NAME="myaction" VALUE="showtour1">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Next">
	</FORM>
	<FORM NAME="CancelForm" ACTION="$CGI" METHOD="POST">
	<INPUT TYPE="HIDDEN" NAME="UserId" VALUE="$UserId">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Cancel">
	</FORM>
	    </TD>
	  </TR>
	</TABLE>
	<BR>
	</DIV>
	<BR>
	endOfResponse1
		&printHlxFooter;
	}


	sub printNewUserForm
	{
		my $mesg = shift() || "";
		my $newuserscript = <<endOfScript;
	function verifyFields() {
		var errorText = "";
		with (document.user_form) {
			if (FirstName.value == "")
				errorText += "Your First Name must be filled out.\\n";
			if (LastName.value == "")
				errorText += "Your Last Name must be filled out.\\n";
			if (Phone.value == "")
				errorText += "Your Phone Number must be filled out.\\n";
			if (Email.value == "")
				errorText += "Your Email Address must be filled out.\\n";
			if (Address1.value == "")
				errorText += "Your Address must be filled out.\\n";
			if (City.value == "")
				errorText += "Your City must be filled out.\\n";
			if (State.value == "")
				errorText += "Your State must be filled out.\\n";
			if (Zip.value == "")
				errorText += "Your Zip must be filled out.\\n";
		}
		if (errorText != "") {
			alert(errorText);
			return false;
		}
		return true;
	}
	endOfScript

		&printHlxHeader("New User","New User",$newuserscript,
						"Step 1: Please enter your information into the form<BR><BR>\nStep 2: Click on the appropriate button to...<BR>\n<UL STYLE=\"margin-left:8px; color:#000080\">\n<LI>View Helyxzion Viewer Tour\n<LI>Buy a product license\n<LI>Request contact from salesperson\n</UL>");
		print <<endOfNewUserForm;
	$mesg<FORM NAME="user_form" ACTION="$CGI" METHOD="POST" onSubmit="return verifyFields()">
	<DIV CLASS="pageGrey" ALIGN="CENTER">
	<BR>
	<TABLE BORDER="0" CELLPADDING="2" CELLSPACING="0">
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Greeting:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="RADIO" NAME="Sal" VALUE="Mr"> Mr.<BR>
	<INPUT TYPE="RADIO" NAME="Sal" VALUE="Ms"> Ms.<BR>
	<INPUT TYPE="RADIO" NAME="Sal" VALUE="Dr"> Dr.<BR>
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>First Name:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="FirstName" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Last Name:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="LastName" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Title:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Title" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Company/School:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Company" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Company Type:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<SELECT NAME="CompanyType" SIZE="1">
	<OPTION VALUE="0">BioTech</OPTION>
	<OPTION VALUE="1">Education</OPTION>
	<OPTION VALUE="2">Research / Development</OPTION>
	<OPTION VALUE="3">Pharmaceutical</OPTION>
	<OPTION VALUE="4">Medical</OPTION>
	<OPTION VALUE="4">Nano Technology</OPTION>
	<OPTION VALUE="5">Space Technology</OPTION>
	<OPTION VALUE="6">Nerf Herding</OPTION>
	</SELECT>
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Email:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Email" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Phone:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Phone" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Address 1:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Address1" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Address 2:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Address2" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>City:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="City" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>State:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="State" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Zip:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="Zip" VALUE="" SIZE="7">
	    </TD>
	  </TR>
	  <TR>
	    <TD COLSPAN="2" ALIGN="RIGHT" VALIGN="TOP">
	<INPUT TYPE="HIDDEN" NAME="myaction" VALUE="createuser">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Buy a license"><BR>
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="View Helyxzion Viewer Tour"><BR>
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Request contact from salesperson"><BR>
	    </TD>
	  </TR>
	</TABLE>
	</FORM>
	<BR>
	</DIV>
	<BR>
	endOfNewUserForm
		&printHlxFooter;
	}

	sub printResponse
	{
		my $mesg = shift() || "No data for page.";
		my $tit = shift() || "";
		my $scr = shift() || "";
		&printHlxHeader("CGI Response","CGI Response","","There is a CGI Generated response.");
		### Insert UI Here
		print <<endOfGenericResponse;
	$mesg
	endOfGenericResponse
		&printHlxFooter;
	}

	sub printError
	{
		my $err = shift();
		$err = "Error:<BR>\n<B><FONT COLOR=\"RED\">$err</FONT></B><BR>\n";
		&printResponse($err);
	}

	sub printPage
	{
		my $file = shift();
		open (INFILE, "$file") || &printError("Couldn't open file.");
		#print "loading $file<BR>\n";
		while ($line = <INFILE>) {
			print $line;
		}
		close(INFILE);
		exit;
	}

	sub printSalesPage
	{
		my $FirstName = $q->param('FirstName') || "";
		my $LastName = $q->param('LastName') || "";
		my $Title = $q->param('Title') || "";
		my $Company = $q->param('Company') || "";
		my $CompanyType = $q->param('CompanyType') || "0";
		my $Email = $q->param('Email') || "";
		my $Phone = $q->param('Phone') || "";
		my $Address1 = $q->param('Address1') || "";
		my $Address2 = $q->param('Address2') || "";
		my $City = $q->param('City') || "";
		my $State = $q->param('State') || "";
		my $Zip = $q->param('Zip') || "";
		$FirstName =~ s/(['])/\\$1/g;
		$LastName =~ s/(['])/\\$1/g;
		$Title =~ s/(['])/\\$1/g;
		$Company =~ s/(['])/\\$1/g;
		$Email =~ s/(['])/\\$1/g;
		$Phone =~ s/(['])/\\$1/g;
		$Address1 =~ s/(['])/\\$1/g;
		$Address2 =~ s/(['])/\\$1/g;
		$City =~ s/(['])/\\$1/g;
		$State =~ s/(['])/\\$1/g;
		$Zip =~ s/(['])/\\$1/g;

		&printHlxHeader("Contact Sales","Contact Sales Department","","Use this form to formally request a contact with a salesperson, or email <A HREF=\"mailto:sales\@helyxzion.com\">sales\@helyxzion.com</A>");
		### Insert UI Here
		print <<endOfResponse1;
	Thanks $FirstName $LastName,<BR>
	Your account is on file.<BR>
	An Helyxzion sales representative will contact you within three business days at $Phone.<BR>
	<BR>
	First Name: $FirstName<BR>
	Last Name: $LastName<BR>
	Title: $Title<BR>
	Company: $Company<BR>
	Email: $Email<BR>
	Phone: $Phone<BR>
	Address 1: $Address1<BR>
	Address 2: $Address2<BR>
	City: $City<BR>
	State: $State<BR>
	Zip: $Zip<BR>
	endOfResponse1
		&printHlxFooter;
	}

	sub printLegalPage
	{
		&printHlxHeader("New User - Step 3","New User - Step 3","","A Legal Word...");
		&printHlxFooter;
	}

	sub printActPage
	{
		my $mesg = shift() || "";
		my $newuserscript = <<endOfScriptB;
	function verifyFields() {
	    var errorText = "";
	    with (document.account_form) {
	        if (UserName.value == "")
	            errorText += "Your User Name must be filled out.\\n";
	        if (Password.value == "")
	            errorText += "You must create a password.\\n";
	        else if (Password.value.match(/[^a-zA-Z0-9]/))
	            errorText += "You must create an alphanumeric password.\\n";
	        else if (Password.value.length < 4)
	            errorText += "Your password must be 4 or more characters in length.\\n";
	        else if (Password.value != ConfirmPassword.value)
	            errorText += "Your Passwords did not match.\\n";
			if (LicenseType.value < 0 || LicenseType.value > 5)
				errorText += "You must select a license type.\\n";
	    }
	    if (errorText != "") {
	        alert(errorText);
	        return false;
	    }
	    return true;
	}

	function cancelForm() {
		document.location = "$CGI?action=mainmenu";
	}
	endOfScriptB

		my $FirstName = $q->param('FirstName');
		my $LastName = $q->param('LastName');
		&printHlxHeader("New User - Buy License","New User - Buy License",$newuserscript,
	"<B>Welcome, $FirstName $LastName</B><BR>\n<BR>\nTo use the Helyxzion Viewer, you must create a username and password, and select which license you wish to purchase.  Then click the 'Next' button.");
		my @SEL;
		my $LicenseType = int($q->param('LicenseType')) || 2;
		foreach my $i (1..3) {
			if ($i == $LicenseType) {
				$SEL[$i] = "CHECKED";
			} else {
				$SEL[$i] = "";
			}
		}
		### Insert UI Here
		print <<endOfResponse1;
	<FONT COLOR="RED"><B>$mesg</B></FONT>
	<DIV CLASS="pageGrey" ALIGN="CENTER">
	<BR>
	<FORM NAME="account_form" ACTION="$CGI" METHOD="POST" onSubmit="return verifyFields()">
	<TABLE BORDER="0" CELLPADDING="2" CELLSPACING="0">
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>User Name:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="TEXT" NAME="UserName" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Password:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="PASSWORD" NAME="Password" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>Confirm Password:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	<INPUT TYPE="PASSWORD" NAME="ConfirmPassword" VALUE="" SIZE="35">
	    </TD>
	  </TR>
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>License Type:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	Type 1 (\$5,000.00): <INPUT TYPE="RADIO" NAME="LicenseType" VALUE="1" ${SEL[1]}><BR>
	Type 2 (\$10,000.00): <INPUT TYPE="RADIO" NAME="LicenseType" VALUE="2" ${SEL[2]}><BR>
	Type 3 (\$50,000.00): <INPUT TYPE="RADIO" NAME="LicenseType" VALUE="3" ${SEL[3]}><BR>
	    </TD>
	  </TR>
	</TABLE>
	<INPUT TYPE="HIDDEN" NAME="UserId" VALUE="$UserId">
	<INPUT TYPE="HIDDEN" NAME="myaction" VALUE="buylicense1">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Next">
	</FORM>
	<FORM NAME="CancelForm" ACTION="$CGI" METHOD="POST">
	<INPUT TYPE="HIDDEN" NAME="UserId" VALUE="$UserId">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Cancel">
	</FORM>
	<BR>
	</DIV>
	<BR>
	endOfResponse1
		&printHlxFooter;
	}

	sub printBuyLicense
	{
		my $newuserscript = <<endOfScriptB;
	function verifyFields() {
	    var errorText = "";
	    with (document.account_form) {
			if (LicenseType.value < 0 || LicenseType.value > 5)
				errorText += "You must select a license type.\\n";
	    }
	    if (errorText != "") {
	        alert(errorText);
	        return false;
	    }
	    return true;
	}

	function cancelForm() {
		document.location = "$CGI?action=mainmenu";
	}
	endOfScriptB

		my $FirstName = $q->param('FirstName');
		my $LastName = $q->param('LastName');
		&printHlxHeader("New User - Buy License","New User - Buy License",$newuserscript,
	"<B>Welcome, $FirstName $LastName</B><BR>\n<BR>\nTo use the Helyxzion Viewer, you must create a username and password, and select which license you wish to purchase.  Then click the 'Next' button.");
		my @SEL;
		my $LicenseType = int($q->param('LicenseType')) || 2;
		foreach my $i (1..3) {
			if ($i == $LicenseType) {
				$SEL[$i] = "CHECKED";
			} else {
				$SEL[$i] = "";
			}
		}
		### Insert UI Here
		
		my $mesg = shift()."<BR>\n" || "";
		print <<endOfBuyLic1;
	<FONT COLOR="RED"><B>$mesg</B></FONT>
	<DIV CLASS="pageGrey" ALIGN="CENTER">
	<BR>
	<FORM NAME="buylicense_form" ACTION="$CGI" METHOD="POST" onSubmit="//return verifyFields()">
	<TABLE BORDER="0" CELLPADDING="2" CELLSPACING="0">
	  <TR>
	    <TD ALIGN="RIGHT" VALIGN="TOP">
	<B>License Type:</B>
	    </TD>
	    <TD ALIGN="LEFT" VALIGN="TOP">
	Type 1 (\$5,000.00): <INPUT TYPE="RADIO" NAME="LicenseType" VALUE="1" ${SEL[1]}><BR>
	Type 2 (\$10,000.00): <INPUT TYPE="RADIO" NAME="LicenseType" VALUE="2" ${SEL[2]}><BR>
	Type 3 (\$50,000.00): <INPUT TYPE="RADIO" NAME="LicenseType" VALUE="3" ${SEL[3]}><BR>
	    </TD>
	  </TR>
	</TABLE>
	<INPUT TYPE="HIDDEN" NAME="UserId" VALUE="$UserId">
	<INPUT TYPE="HIDDEN" NAME="myaction" VALUE="userbuylicense">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Next">
	</FORM>
	<FORM NAME="CancelForm" ACTION="$CGI" METHOD="POST">
	<INPUT TYPE="HIDDEN" NAME="UserId" VALUE="$UserId">
	<INPUT TYPE="SUBMIT" NAME="Sub" VALUE="Cancel">
	</FORM>
	<BR>
	</DIV>
	<BR>
	endOfBuyLic1
		&printHlxFooter;
	}

	sub check_login
	{
		## Check user login with database
		my $printHeader = shift();
		my $myUserName = $q->param('UserName') || "";
		my $myPassword = $q->param('Password') || "";
		my $dba = DBI->connect('DBI:mysql:usertable;mysql_socket=/private/var/mysql/mysql.sock','root','biteme') or &printError("Couldn't connect to database: ".DBI->errstr);
		my $sqlStr0 = "SELECT Username, pword, UID, description FROM users WHERE Username = '$myUserName'";
		my $stb = $dba->prepare($sqlStr0);
		$stb->execute or &printError("I Couldn't execute statement: ".$stb->errstr);
		my @found = $stb->fetchrow();
		$stb->finish;
		$dba->disconnect;
		if ($#found > 0) {
			print STDERR "Checking to see if $myUserName eq $found[0] && $myPassword eq $found[1]\n";
			if ($myPassword eq $found[1]) {
				$USER_IDENT= $found[2];
				print STDERR "\nFound userident = $USER_IDENT\n";
				if (!$HEADER) {
					my $cookie = &set_cookie($COOKIENAME,"$USER_IDENT","+8h");
					print $q->header( -cookie=>$cookie );
					$HEADER = 1;
					print STDERR "Printed cookie header $cookie<BR>\n";
				} else {
					print STDERR "Already printed HEADER\n";
				}
				return 1;
			} else {
				print STDERR "Bad Password.\n";
				return 0;
				#&printLoginPage("<FONT COLOR=RED><B>Password incorrect.</B></FONT>");
			}
		} else {
			print STDERR "Account does not exist.\n";
			return 0;
			#&printLoginPage("<FONT COLOR=RED><B>Account '$myUserName' does not exist.</B></FONT>");
		}

	}

	sub registered
	{
		my $printHeader = shift();
		$USER_IDENT = &check_cookie($COOKIENAME);
		print STDERR "\n\n\n\n\n------- In $CGI, found $COOKIENAME = $USER_IDENT\n";
		return 0 unless ($USER_IDENT);
		#return 0 unless (&check_cookie($COOKIENAME));
		print STDERR "In registered and printHeader = $printHeader.\n";
		if ($printHeader) {
			my $cookie = &set_cookie($COOKIENAME,"$USER_IDENT","+8h");
			print STDERR "Printing registered cookie header with $cookie\n";
			print $q->header( -cookie=>$cookie );
			$HEADER = 1;
		}
		return 1;
	}

	sub logout
	{
	    my $cookie = &set_cookie($COOKIENAME,'loggingout','-2d');
	    my $rurl = $q->url();
		print STDERR ">>>>>>> Printing logout redirect $rurl and with $cookie.\n";
	    print $q->redirect(-uri=>$rurl,
			       -cookie=>"$cookie");
	    exit;
	}

	sub set_cookie
	{
	    my $name = shift;
	    my $value = shift;
	    my $expire = shift;

		print STDERR "name = $name and $value and $expire \n";
	    $expire = '+y' unless ($expire);

	    my $cookie = $q->cookie(-name=>$name,
	                            -value=>$value,
	                            -expires=>$expire,
	                            -path=>'/',
	                            -secure=>0);
	                            #-domain=>$THISDOMAIN,

		#print STDERR "Setting cookie to $cookie \n";
	    return $cookie;
	}

	sub check_cookie
	{
	    my $name = shift;
	    my @cookies = $q->cookie("$name");
	    foreach $coo (@cookies) {
		print STDERR $coo."\n";
	    }
	    #return @cookies;
		return $cookies[0];
	}

	sub dbconnect {
	    return DBI->connect('DBI:mysql:userdata;mysql_socket=/private/var/mysql/mysql.sock','mysql','mysql') or die "Couldn't connect to database: ".DBI->errstr;
	}


}
